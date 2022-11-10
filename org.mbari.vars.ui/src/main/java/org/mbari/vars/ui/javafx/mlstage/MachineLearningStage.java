package org.mbari.vars.ui.javafx.mlstage;


import com.jfoenix.controls.JFXButton;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.mbari.imgfx.imageview.ImagePaneController;
import org.mbari.imgfx.roi.Localization;
import org.mbari.imgfx.roi.RectangleView;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.javafx.Icons;
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
    private VBox rightPane = new VBox();
    private final UIToolBox toolBox;
    private ObjectProperty<Color> fill = new SimpleObjectProperty<>(Color.valueOf("#FF980030"));

    public MachineLearningStage(UIToolBox toolBox) {
        this.toolBox = toolBox;
        init();
    }

    private void init() {
        rightPane.setPadding(new Insets(15));

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
        scrollPane.setContent(rightPane);
        root.setRight(scrollPane);

        var expander = new Region();
        HBox.setHgrow(expander, Priority.ALWAYS);
        var icon = Icons.SAVE.standardSize();
        var button = new JFXButton(null, icon);
        var hbox = new HBox(expander, button);
        button.setOnAction(event -> {
            log.atInfo().log("Save clicked");
        });
        root.setBottom(hbox);

        Scene scene = new Scene(root);
        scene.getStylesheets()
                .addAll(toolBox.getStylesheets());
        setScene(scene);
    }

    public void setImage(Image image) {
        imagePaneController.getView().setImage(image);
    }

    public BorderPane getRoot() {
        return (BorderPane) getScene().getRoot();
    }

    public ImagePaneController getImagePaneController() {
        return imagePaneController;
    }

    public synchronized void setLocalizations(Collection<Localization<RectangleView, ImageView>> locs) {
        rightPane.getChildren().clear();
        localizations.forEach(loc -> loc.setVisible(false));
        localizations.clear();
        localizations.addAll(locs);
        localizations.sort(Comparator.comparing(Localization::getLabel));
        localizations.forEach(loc -> {
            var editor = new LocalizationEditorController(toolBox, loc);
            rightPane.getChildren().add(editor.getRoot());
            loc.getDataView().setColor(fill.get());
            loc.setVisible(true);
        });
    }
}
