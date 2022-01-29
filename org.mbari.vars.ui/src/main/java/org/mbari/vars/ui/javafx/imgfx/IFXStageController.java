package org.mbari.vars.ui.javafx.imgfx;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.mbari.vars.core.util.AsyncUtils;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Image;
import org.mbari.vars.services.model.ImageReference;
import org.mbari.vars.services.model.Media;
import org.mbari.vars.ui.UIToolBox;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IFXStageController {

    private Stage stage;

    private final IFXToolBox toolBox;
    private final IFXPaneController paneController;
    private final BooleanProperty visible = new SimpleBooleanProperty();

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

        toolBox.getUIToolBox()
                .getData()
                .mediaProperty()
                .addListener((obs, oldv, newv) -> {
                    if (getStage().isShowing()) {
                        setMedia(newv);
                    }
                });


    }

    public Stage getStage() {
        if (stage == null) {
            stage = new Stage();
            var borderPane = paneController.getAnnotationPaneController().getPane();
            var scene = new Scene(borderPane);
            scene.getStylesheets().addAll(toolBox.getStylesheets());
            stage.setScene(scene);
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
        setMedia(media);
        Platform.runLater(() -> getStage().show());
    }

    private void hide() {
        if (stage != null) {
            stage.hide();
        }
    }

    private void setMedia(Media media) {
        if (media == null) {
            toolBox.getData()
                    .getImages()
                    .setAll(Collections.emptyList());
        }
        else {
            toolBox.getUIToolBox()
                    .getServices()
                    .getAnnotationService()
                    .findImagesByVideoReferenceUuid(media.getVideoReferenceUuid())
                    .thenAccept(toolBox.getData().getImages()::setAll);



        }
    }



}
