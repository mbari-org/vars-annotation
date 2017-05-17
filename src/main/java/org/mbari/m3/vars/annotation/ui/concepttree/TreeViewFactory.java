package org.mbari.m3.vars.annotation.ui.concepttree;

import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import org.mbari.m3.vars.annotation.model.Concept;
import org.mbari.m3.vars.annotation.model.ConceptMedia;
import org.mbari.m3.vars.annotation.services.ConceptService;
import org.mbari.m3.vars.annotation.ui.shared.FilterableTreeItem;
import org.mbari.m3.vars.annotation.ui.shared.ImageStage;

import java.net.URL;

/**
 * @author Brian Schlining
 * @since 2017-05-15T17:41:00
 */
class TreeViewFactory {

    private ConceptService conceptService;

    TreeViewFactory(ConceptService conceptService) {
        this.conceptService = conceptService;
    }

    TreeView<Concept> build() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem showImage = new MenuItem("Show image");

        ImageStage imageStage = new ImageStage();
        BorderPane imageStageRoot = imageStage.getRoot();
        imageStageRoot.setStyle("-fx-background-color: black");
        contextMenu.getItems().addAll(showImage);

        TreeCellFactory cellFactory = new TreeCellFactory(conceptService);
        TreeView<Concept> tree = new TreeView<>();
        tree.setEditable(false);
        tree.setCellFactory(tv -> cellFactory.build());
        tree.setContextMenu(contextMenu);
        showImage.setOnAction(event -> {
            TreeItem<Concept> item = tree.getSelectionModel().getSelectedItem();
            Concept concept = item.getValue();
            if (concept != null &&
                    concept.getConceptDetails() != null &&
                    concept.getConceptDetails().getMedia() != null) {

                concept.getConceptDetails()
                        .getMedia()
                        .stream()
                        .filter(ConceptMedia::isPrimary)
                        .findFirst()
                        .ifPresent(m -> {
                            Image image = new Image(m.getUrl().toExternalForm());
                            imageStage.setImage(image);
                            imageStage.show();
                        });
            }
        });

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

    private FilterableTreeItem<Concept> buildTreeItem(Concept concept, FilterableTreeItem<Concept> parent) {
        FilterableTreeItem<Concept> item = new FilterableTreeItem<>(concept);
        if (parent != null) {
            //parent.getChildren().add(item);
            parent.getInternalChildren().add(item);
        }
        concept.getChildren()
                .forEach(c -> buildTreeItem(c, item));
        return item;
    }

}
