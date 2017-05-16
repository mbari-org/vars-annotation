package org.mbari.m3.vars.annotation.ui.concepttree;

import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.mbari.m3.vars.annotation.model.Concept;
import org.mbari.m3.vars.annotation.services.ConceptService;

/**
 * @author Brian Schlining
 * @since 2017-05-15T17:41:00
 */
public class TreeFactory {

    private ConceptService conceptService;

    public TreeFactory(ConceptService conceptService) {
        this.conceptService = conceptService;
    }

    public TreeView<Concept> build() {
        TreeCellFactory cellFactory = new TreeCellFactory(conceptService);
        TreeView<Concept> tree = new TreeView<>();
        tree.setCellFactory(tv -> cellFactory.newTreeCell());
        conceptService.fetchConceptTree()
                .thenApply(root -> {
                    Platform.runLater(() -> {
                        TreeItem<Concept> rootItem = buildTreeItem(root, null);
                        tree.setRoot(rootItem);
                    });
                    return null;
                });

        return tree;
    }

    private TreeItem<Concept> buildTreeItem(Concept concept, TreeItem<Concept> parent) {
        TreeItem<Concept> item = new TreeItem<>(concept);
        if (parent != null) {
            parent.getChildren().add(item);
        }
        concept.getChildren()
                .forEach(c -> buildTreeItem(c, item));
        return item;
    }

}
