package org.mbari.vars.annotation.ui.javafx.imagestage;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annosaurus.sdk.r1.models.Association;
import org.mbari.vars.annotation.services.annosaurus.BoundingBoxes;
import org.mbari.vars.annotation.ui.util.JFXUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class ImageStage2 extends Stage {
    private ImagePaneController imagePaneController;
    private final List<Localization<RectangleView, ImageView>> localizations = new CopyOnWriteArrayList<>();
    private static final Logger log = LoggerFactory.getLogger(ImageStage2.class);

    ObjectProperty<Color> fill = new SimpleObjectProperty<>(Color.valueOf("#FF980030"));

    public ImageStage2() {
        init();
    }

    public ImageStage2(StageStyle stageStyle) {
        super(stageStyle);
        init();
    }

    private void init() {

        setWidth(400);
        setHeight(400);

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
                    .map(a -> BoundingBoxes.fromAssociation(annotation.getConcept(), a, imagePaneController))
                    .flatMap(Optional::stream)
                    .toList();
            if (!boundingBoxes.isEmpty()) {
                log.atDebug().log(() -> "Displaying " + boundingBoxes.size() + " for " + annotation.getConcept());
                localizations.addAll(boundingBoxes);
                localizations.forEach(loc -> {
                    loc.getDataView().getView().setFill(fill.get());
                    loc.setVisible(true);
                });
            }
        });

    }

    private void clearLocalizations() {


        var locs = new ArrayList<>(localizations);
        localizations.clear();
        JFXUtilities.runOnFXThread(() -> {
            imagePaneController.getView().setImage(null);
            // setVisible(false) actually removes all the localization nodes from their parents
            locs.forEach(loc -> loc.setVisible(false));
        });

    }
}
