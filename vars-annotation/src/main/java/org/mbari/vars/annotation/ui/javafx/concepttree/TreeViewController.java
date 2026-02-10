package org.mbari.vars.annotation.ui.javafx.concepttree;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import org.mbari.vars.annotation.etc.gson.Gsons;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.oni.sdk.r1.models.Concept;
import org.mbari.vars.oni.sdk.r1.models.ConceptMedia;
import org.mbari.vars.annotation.ui.javafx.shared.ImageStage;
import org.mbari.vars.annotation.etc.jdk.Loggers;
import org.mbari.vars.annotation.ui.javafx.shared.FilterableTreeItem;
import org.mbari.vars.annotation.ui.messages.ReloadServicesMsg;

/**
 * @author Brian Schlining
 * @since 2017-06-01T16:21:00
 */
public class TreeViewController {

    private ContextMenu contextMenu;
    private ImageStage imageStage;
    private TreeView<Concept> treeView;
    private final Loggers log = new Loggers(getClass());
    private final UIToolBox toolBox;

    public TreeViewController(UIToolBox toolBox) {
        this.toolBox = toolBox;
        toolBox.getEventBus()
                .toObserverable()
                .ofType(ReloadServicesMsg.class)
                .subscribe(evt -> loadRoot());
    }

    private FilterableTreeItem<Concept> buildTreeItem(Concept concept, FilterableTreeItem<Concept> parent) {
        FilterableTreeItem<Concept> item = new FilterableTreeItem<>(concept);
        if (parent != null) {
            parent.getSourceChildren().add(item);
        }
        concept.getChildren()
                .forEach(c -> buildTreeItem(c, item));
        return item;
    }

    public ImageStage getImageStage() {
        if (imageStage == null) {
            imageStage = new ImageStage();
            ImageStage imageStage = new ImageStage();
            BorderPane imageStageRoot = imageStage.getRoot();
            imageStageRoot.setStyle("-fx-background-color: black");
        }
        return imageStage;
    }

    private ContextMenu getContextMenu() {
        if (contextMenu == null) {
            contextMenu = new ContextMenu();
            MenuItem showImage = new MenuItem("Show image");
            contextMenu.getItems().addAll(showImage);
            showImage.setOnAction(event -> {
                TreeItem<Concept> item = getTreeView().getSelectionModel().getSelectedItem();
                Concept concept = item.getValue();

                log.atDebug().log(() -> {
                    Gson gson = Gsons.SNAKE_CASE_GSON;
                    return "Showing image for concept: " + gson.toJson(concept);
                });
                var conceptService = toolBox.getServices().conceptService();
                conceptService.findDetails(concept.getName())
                        .thenAccept(opt -> {
                            if (opt.isEmpty()) {
                                log.atInfo().log(() -> "No details found for concept: " + concept.getName());
                                return;
                            }
//                            opt.ifPresent(cd -> log.atWarn().log(() -> "Found details: " + gson.toJson(cd)));
                            opt.flatMap(cd -> cd.getMedia()
                                    .stream()
                                    .filter(ConceptMedia::isPrimary)
                                    .findFirst()).ifPresent(m -> {
                                        Platform.runLater(() -> {
                                            Image image = new Image(m.getUrl().toExternalForm());
                                            getImageStage().setImage(image);
                                            getImageStage().show();
                                        });
                                    });
                        });
            });
        }
        return contextMenu;
    }

    public TreeView<Concept> getTreeView() {
        if (treeView == null) {
            TreeCellFactory cellFactory = new TreeCellFactory(toolBox);
            treeView = new TreeView<>();
            treeView.getStyleClass().add("concepttree-treeview");
            treeView.setEditable(false);
            treeView.setCellFactory(tv -> cellFactory.build());
            treeView.setContextMenu(getContextMenu());
            loadRoot();
        }
        return treeView;
    }

    private void loadRoot() {
        var conceptService = toolBox.getServices().conceptService();
        log.atDebug().log(() -> "Using concept service: " + conceptService + " to load root");
        conceptService
                .findRoot()
                .thenCompose(root -> conceptService.findPhylogenyDown(root.getName()))
                .thenAccept(opt -> {
                    if (opt == null || opt.isEmpty()) {
                        log.atError().log(() -> "Unable to load full Knowledgebase phylogeny.");
                        throw new RuntimeException("Unable to load full Knowledgebase phylogeny tree.");
                    }
                    Platform.runLater(() -> {
                        var root = opt.get();
                        log.atDebug().log(() -> "Using root '" + root.getName() + "' to build tree");
                        TreeItem<Concept> rootItem = buildTreeItem(root, null);
                        treeView.setRoot(rootItem);
                    });
                });
    }
}
