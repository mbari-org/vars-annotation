package org.mbari.vars.ui.javafx.mlstage;


import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.mbari.imgfx.imageview.ImagePaneController;
import org.mbari.imgfx.roi.Localization;
import org.mbari.imgfx.roi.RectangleView;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.javafx.imagestage.ImageStage2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MachineLearningStage extends Stage {

    private ImagePaneController imagePaneController;
    private final List<Localization<RectangleView, ImageView>> localizations = new CopyOnWriteArrayList<>();
    private static final Logger log = LoggerFactory.getLogger(ImageStage2.class);
    private VBox leftPane = new VBox();
    private final UIToolBox toolBox;

    public MachineLearningStage(UIToolBox toolBox) {
        this.toolBox = toolBox;
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

        var scrollPane = new ScrollPane();
        scrollPane.setContent(leftPane);

        Scene scene = new Scene(root);
        scene.setFill(Color.BLACK);
        setScene(scene);
    }


    public BorderPane getRoot() {
        return (BorderPane) getScene().getRoot();
    }

    public synchronized void setLocalizations(Collection<Localization<RectangleView, ImageView>> locs) {
        leftPane.getChildren().clear();
        localizations.clear();
        localizations.addAll(locs);
        localizations.sort(Comparator.comparing(Localization::getLabel));
        localizations.forEach(loc -> {
            var editor = new LocalizationEditorController(toolBox, loc);
            leftPane.getChildren().add(editor.getRoot());
        });
    }
}
