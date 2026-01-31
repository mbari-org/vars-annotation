package org.mbari.vars.annotation.ui.javafx.concepttree;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.javafx.shared.FilterableTreeItem;
import org.mbari.vars.oni.sdk.r1.models.Concept;


import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-06-01T16:20:00
 */
public class SearchTreePaneController {

    private final UIToolBox toolBox;
    private ResourceBundle uiBundle;
    private BorderPane root;
    private TextField textField;
    private TreeViewController treeViewController;

    public SearchTreePaneController(UIToolBox toolBox, ResourceBundle uiBundle) {
        this.toolBox = toolBox;
        this.uiBundle = uiBundle;

        // TODO listen to ShowConceptInTreeViewMsg then select/scrollTo that node
    }

    public SearchTreePaneController(UIToolBox toolBox) {
        this(toolBox, toolBox.getI18nBundle());
    }

    public BorderPane getRoot() {
        if (root == null) {
            HBox.setHgrow(getTextField(), Priority.ALWAYS);
            root = new BorderPane();
            root.setCenter(getTreeView());
            root.setTop(getTextField());
        }
        return root;
    }

    public void setSearchText(String searchText) {
        Platform.runLater(() -> getTextField().setText(searchText));
    }

    private TreeView<Concept> getTreeView() {
        if (treeViewController == null) {
            treeViewController = new TreeViewController(toolBox);
            TreeView<Concept> treeView = treeViewController.getTreeView();

            /*
             Often Kyra will search for a known term, say malacostrea, then browse
             to see what's underneath it. Maybe add "show this node" item to the
             context menu that clears the search, , scrolls to the selected node and
             expands all children.
            */
            ContextMenu contextMenu = treeView.getContextMenu();
            MenuItem showChildren = new MenuItem("Show All Children");
            contextMenu.getItems().addAll(showChildren);
            showChildren.setOnAction(event -> {
                // Get node
                final TreeItem<Concept> selectedItem = treeView.getSelectionModel().getSelectedItem();
                getTextField().setText("");
                treeView.getSelectionModel().select(selectedItem);
                expand(selectedItem);
            });

            treeView.rootProperty().addListener(((observable, oldValue, newValue) -> {
                if (oldValue != null) {
                    var oldRoot = (FilterableTreeItem<Concept>) oldValue;
                    oldRoot.predicateProperty().unbind();
                }
                if (newValue != null) {
                    var newRoot = (FilterableTreeItem<Concept>) newValue;
                    TextField tf = getTextField();
                    newRoot.predicateProperty().bind(Bindings.createObjectBinding(() -> {
                        if (tf.getText() == null || tf.getText().isEmpty()) {
                            return (Concept c) -> true;
                        }
                        else {
                            return (Concept c) -> {
                                String t = c.getName();
                                List<String> alternativeNames = c.getAlternativeNames();
                                if (alternativeNames != null && !alternativeNames.isEmpty()) {
                                    t = t + String.join("", alternativeNames);
                                }
                                return t.toLowerCase().contains(tf.getText().toLowerCase());
                            };

                        }
                    }, tf.textProperty()));
                }
            }));
        }
        return treeViewController.getTreeView();
    }

    private TextField getTextField() {
        if (textField == null) {
            textField = new TextField();
            textField.getStyleClass().add("concepttree-textfield");
            //TextField textField = new TextField();
            textField.setPromptText(uiBundle.getString("concepttree.prompt"));
            textField.textProperty().addListener((obs, oldValue, newValue) -> {
                TreeItem<Concept> root = getTreeView().getRoot();
                if (newValue.length() > oldValue.length()) {
                    if (root != null) {
                        expand(root);
                    }
                }
                if (newValue.isEmpty()) {
                    if (root != null) {
                        collapse(root);
                    }
                }
            });
            textField.setOnKeyPressed(event -> {
                KeyCode keyCode = event.getCode();
                if (keyCode.equals(KeyCode.UP)) {
                    selectNextInTree(-1);
                }
                else if (keyCode.equals(KeyCode.DOWN)) {
                    selectNextInTree(1);
                }
            });
        }
        return textField;
    }

    private void selectNextInTree(int i) {
        TreeView<Concept> treeView = getTreeView();
        int idx = treeView.getSelectionModel().getSelectedIndex();
        treeView.getSelectionModel().select(idx + i);
        treeView.scrollTo(idx + i);
    }

    private static void expand(TreeItem<?> item) {
        if(item != null && !item.isLeaf()){
            item.setExpanded(true);
            for(TreeItem<?> child :item.getChildren()){
                expand(child);
            }
        }
    }

    private static void collapse(TreeItem<?> item) {
        if(item != null && !item.isLeaf()){
            item.setExpanded(false);
            for(TreeItem<?> child :item.getChildren()){
                collapse(child);
            }
        }
    }
}
