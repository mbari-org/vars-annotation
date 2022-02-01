package org.mbari.vars.ui.javafx.imgfx;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.mbari.vars.services.model.Image;
import org.mbari.vars.services.model.Media;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.util.URLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class IFXStageController {

    private static final String PREF_KEY_WIDTH = "stage-width";
    private static final String PREF_KEY_HEIGHT = "stage-height";
    private static final int MIN_DIMENSION = 400;
    private static final Logger log = LoggerFactory.getLogger(IFXStageController.class);

    private Stage stage;

    private final IFXToolBox toolBox;
    private final IFXPaneController paneController;
    private final BooleanProperty visible = new SimpleBooleanProperty();
    private final Comparator<Image> imageComparator =
            Comparator.comparing(a -> URLUtils.filename(a.getUrl()));

    /** This handles when a user changes the video or data set they are looking at. */
    private MediaLifecycleDecorator mediaLifecycleDecorator;

    public IFXStageController(UIToolBox toolBox) {
        this.toolBox = initializeToolBox(toolBox);
        this.paneController = new IFXPaneController(this.toolBox);
        init();
    }

    private void init() {
        visibleProperty().addListener((obs, oldv, newv) -> {
            if (newv) {
                show();
            }
            else {
                hide();
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(this::save));
    }

    public Stage getStage() {
        if (stage == null) {
            stage = new Stage();
            var borderPane = paneController.getAnnotationPaneController().getPane();
            var scene = new Scene(borderPane);
            scene.getStylesheets().addAll(toolBox.getStylesheets());
            stage.setScene(scene);
            mediaLifecycleDecorator = new MediaLifecycleDecorator(stage, toolBox);
            load();
        }
        return stage;
    }

    private static IFXToolBox initializeToolBox(UIToolBox toolBox) {
        return new IFXToolBox(toolBox,
                new IFXData(),
                new org.mbari.imgfx.etc.rx.EventBus(),
                List.of("imgfx.css"));
    }

    public boolean isVisible() {
        return visible.get();
    }

    public BooleanProperty visibleProperty() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible.set(visible);
    }

    private void show() {
        var media = toolBox.getUIToolBox().getData().getMedia();
        mediaLifecycleDecorator.setMedia(media);
        Platform.runLater(() -> getStage().show());
    }

    private void hide() {
        if (stage != null) {
            stage.hide();
            mediaLifecycleDecorator.setMedia(null);
        }
    }

    private void save() {
        var prefs = Preferences.userNodeForPackage(IFXStageController.class);
        var width = Math.max(stage.getWidth(), MIN_DIMENSION);
        var height = Math.max(stage.getHeight(), MIN_DIMENSION);
        prefs.putDouble(PREF_KEY_WIDTH, width);
        prefs.putDouble(PREF_KEY_HEIGHT, height);
    }

    private void load() {
        var prefs = Preferences.userNodeForPackage(IFXStageController.class);
        var defaultWidth = Math.max(stage.getWidth(), MIN_DIMENSION);
        var defaultHeight = Math.max(stage.getHeight(), MIN_DIMENSION);
        var width = prefs.getDouble(PREF_KEY_WIDTH, defaultWidth);
        var height = prefs.getDouble(PREF_KEY_HEIGHT, defaultHeight);
        stage.setWidth(width);
        stage.setHeight(height);
    }



}
