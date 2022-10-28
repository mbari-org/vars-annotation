package org.mbari.vars.ui.javafx.imagestage;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.mbari.imgfx.imageview.ImagePaneController;
import org.mbari.imgfx.roi.Localization;
import org.mbari.imgfx.roi.RectangleView;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;
import org.mbari.vars.ui.util.JFXUtilities;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class ImageStage2 extends Stage {
    private ImagePaneController imagePaneController;
    private final List<Localization<RectangleView, ImageView>> localizations = new CopyOnWriteArrayList<>();

    public ImageStage2() {
        init();
    }

    public ImageStage2(StageStyle stageStyle) {
        super(stageStyle);
        init();
    }

    private void init() {

        var imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);

        imagePaneController = new ImagePaneController(imageView);
        var pane = imagePaneController.getPane();

        BorderPane root = new BorderPane(pane);
        root.widthProperty()
                .addListener((obs, oldv, newv) -> pane.setPrefWidth(newv.doubleValue()));
        root.heightProperty()
                .addListener((obs, oldv, newv) -> pane.setPrefHeight(newv.doubleValue()));

        Scene scene = new Scene(root);
        scene.setFill(Color.BLACK);
        setScene(scene);

    }

    public BorderPane getRoot() {
        return (BorderPane) getScene().getRoot();
    }

    public void setAnnotation(Annotation annotation, Image image) {
        clearLocalizations();
        JFXUtilities.runOnFXThread(() -> {
            imagePaneController.getView().setImage(image);
            var boundingBoxes = annotation.getAssociations()
                    .stream()
                    .filter(a -> a.getLinkName().equalsIgnoreCase(Association.NAME_LOCALIZATION))
                    .map(a -> BoundingBox.fromAssociation(annotation.getConcept(), a, imagePaneController))
                    .flatMap(Optional::stream)
                    .toList();
            localizations.addAll(boundingBoxes);
            localizations.forEach(v -> v.setVisible(true));
        });

    }

    private void clearLocalizations() {

        var nodes = localizations.stream()
                .map(v -> v.getDataView().getView())
                .toList();
        localizations.clear();
        JFXUtilities.runOnFXThread(() -> {
            imagePaneController.getView().setImage(null);
            imagePaneController.getPane()
                    .getChildren()
                    .removeAll(nodes);
        });

    }
}
