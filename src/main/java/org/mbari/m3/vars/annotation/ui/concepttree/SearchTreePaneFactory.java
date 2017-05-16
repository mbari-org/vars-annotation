package org.mbari.m3.vars.annotation.ui.concepttree;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.mbari.m3.vars.annotation.model.Concept;
import org.mbari.m3.vars.annotation.services.ConceptService;
import org.mbari.m3.vars.annotation.ui.shared.FilterableTreeItem;
import org.mbari.m3.vars.annotation.ui.shared.TreeItemPredicate;

import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-05-16T12:06:00
 */
public class SearchTreePaneFactory {

    // TODO Need an indicator to show that concepts are still loading. Otherwise
    // searches results will be incomplete but the user won't know that the load
    // process is ongoing.

    private ConceptService conceptService;

    public SearchTreePaneFactory(ConceptService conceptService) {
        this.conceptService = conceptService;
    }

    protected BorderPane build() {
        TreeView<Concept> treeView = new TreeViewFactory(conceptService).build();
        TextField textField = new TextField();
        textField.setPromptText("Filter ...");
        textField.textProperty().addListener((obs, oldValue, newValue) -> {
            TreeItem<Concept> root = treeView.getRoot();
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


        /*
          The KB tree is lazy loaded. So we have to listen for when the root is added before
          we can
         */
        ChangeListener<TreeItem<Concept>> rootListener = new ChangeListener<TreeItem<Concept>>() {

            private boolean completed = false;

            @Override
            public void changed(ObservableValue<? extends TreeItem<Concept>> observable, TreeItem<Concept> oldValue, TreeItem<Concept> newValue) {
                if (!completed) {
                    FilterableTreeItem<Concept> root = (FilterableTreeItem<Concept>) newValue;
                    root.predicateProperty().bind(Bindings.createObjectBinding(() -> {
                        if (textField.getText() == null || textField.getText().isEmpty()) {
                            return null;
                        }
                        else {
                            return TreeItemPredicate.create(c -> {
                                String t = c.getName();
                                if (c.getConceptDetails() != null) {
                                    t = t + c.getConceptDetails()
                                            .getAlternateNames()
                                            .stream()
                                            .collect(Collectors.joining());
                                }
                                return t.toLowerCase().contains(textField.getText().toLowerCase());
                            });
                        }
                    }, textField.textProperty()));
                    completed = true;
                }
            }
        };
        treeView.rootProperty().addListener(rootListener);

        HBox.setHgrow(textField, Priority.ALWAYS);
        BorderPane pane = new BorderPane();
        pane.setCenter(treeView);
        pane.setTop(textField);

        return pane;
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
