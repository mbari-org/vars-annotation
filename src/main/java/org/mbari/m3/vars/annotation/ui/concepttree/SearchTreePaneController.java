package org.mbari.m3.vars.annotation.ui.concepttree;

import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.messages.ClearCacheMsg;
import org.mbari.m3.vars.annotation.model.Concept;
import org.mbari.m3.vars.annotation.services.ConceptService;


import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-06-01T16:20:00
 */
public class SearchTreePaneController {

    private ConceptService conceptService;
    private ResourceBundle uiBundle;
    private BorderPane root;
    private TextField textField;
    private TreeViewController treeViewController;

    public SearchTreePaneController(ConceptService conceptService, ResourceBundle uiBundle) {
        this.conceptService = conceptService;
        this.uiBundle = uiBundle;

        // TODO constructor should take toolbox

        // TODO listen to ShowConceptInTreeViewMsg then select/scrollTo that node
    }

    public SearchTreePaneController(UIToolBox toolBox) {
        this(toolBox.getServices().getConceptService(),
                toolBox.getI18nBundle());

        toolBox.getEventBus()
                .toObserverable()
                .ofType(ClearCacheMsg.class)
                .subscribe(msg -> {
                   // TODO implement refresh
                });
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
            treeViewController = new TreeViewController(conceptService);
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

            /*
              The KB tree is lazy loaded. So we have to listen for when the root is added before
              we can bind to it.
             */
            ChangeListener<TreeItem<Concept>> rootListener = new ChangeListener<TreeItem<Concept>>() {

                private boolean completed = false;

                @Override
                public void changed(ObservableValue<? extends TreeItem<Concept>> observable, TreeItem<Concept> oldValue, TreeItem<Concept> newValue) {
                    if (!completed) {
                        TextField tf = getTextField();
                        FilterableTreeItem<Concept> root = (FilterableTreeItem<Concept>) newValue;
                        root.predicateProperty().bind(Bindings.createObjectBinding(() -> {
                            if (tf.getText() == null || tf.getText().isEmpty()) {
                                return null;
                            }
                            else {
                                return TreeItemPredicate.create(c -> {
                                    String t = c.getName();
                                    List<String> alternativeNames = c.getAlternativeNames();
                                    if (alternativeNames != null && !alternativeNames.isEmpty()) {
                                        t = t + alternativeNames.stream().collect(Collectors.joining());
                                    }
//                                    if (c.getConceptDetails() != null) {
//                                        t = t + c.getConceptDetails()
//                                                .getAlternateNames()
//                                                .stream()
//                                                .collect(Collectors.joining());
//                                    }
                                    return t.toLowerCase().contains(tf.getText().toLowerCase());
                                });
                            }
                        }, tf.textProperty()));
                        completed = true;
                    }
                }
            };
            treeView.rootProperty().addListener(rootListener);
        }
        return treeViewController.getTreeView();
    }

    private TextField getTextField() {
        if (textField == null) {
            textField = new JFXTextField();
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
