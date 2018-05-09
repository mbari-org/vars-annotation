package org.mbari.m3.vars.annotation.ui.rectlabel;

import io.reactivex.disposables.Disposable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.events.AnnotationsAddedEvent;
import org.mbari.m3.vars.annotation.events.AnnotationsSelectedEvent;
import org.mbari.m3.vars.annotation.events.MediaChangedEvent;
import org.mbari.m3.vars.annotation.model.Image;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.services.AnnotationService;
import org.mbari.m3.vars.annotation.util.JFXUtilities;
import org.reactivestreams.Subscriber;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Brian Schlining
 * @since 2018-05-04T15:08:00
 */
public class RectLabelStageController {

    private final UIToolBox toolBox;
    private ObservableList images = FXCollections.observableArrayList();
    private Media media;
    private final RectLabelController rectLabelController;
    private Stage stage;
    private Disposable mediaSubscriber;


    public RectLabelStageController(UIToolBox toolBox) {
        this.toolBox = toolBox;
        rectLabelController = RectLabelController.newInstance(toolBox);
        rectLabelController.getRefreshButton()
                .setOnAction(evt -> show());

        toolBox.getEventBus()
                .toObserverable()
                .ofType(AnnotationsSelectedEvent.class)
                .filter(evt -> evt.getEventSource() != rectLabelController)
                .subscribe(selected -> {
                    // TODO if more than one image do nothing
                    // TODO if annotation in annotation list. Select it
                });

        toolBox.getEventBus()
                .toObserverable()
                .ofType(AnnotationsAddedEvent.class)
                .subscribe(evt -> show());
    }

    public void show() {
        Media media = toolBox.getData().getMedia();
        setMedia(media);
        JFXUtilities.runOnFXThread(() -> getStage().show());
        if (mediaSubscriber == null) {
            mediaSubscriber = toolBox.getEventBus()
                    .toObserverable()
                    .ofType(MediaChangedEvent.class)
                    .subscribe(evt -> setMedia(evt.get()));
        }
    }

    private Stage getStage() {
        if (stage == null) {
            stage = new Stage();
            BorderPane root = rectLabelController.getRoot();
            Scene scene = new Scene(root);
            scene.getStylesheets().addAll(toolBox.getStylesheets());
            stage.setScene(scene);
        }
        return stage;
    }

    public void hide() {
        setMedia(null);
        if (stage != null) {
            stage.hide();
        }

        // Disable any eventbus notifications
        if (mediaSubscriber != null) {
            mediaSubscriber.dispose();
        }

    }

    public void setMedia(Media media) {
        this.media = media;
        if (media == null) {
            // TODO turn off eventBus subscriptions
            rectLabelController.setImages(Collections.emptyList());
        }
        else {
            AnnotationService annotationService = toolBox.getServices()
                    .getAnnotationService();
            annotationService.findImagesByVideoReferenceUuid(media.getVideoReferenceUuid())
                    .thenAccept(rectLabelController::setImages);
        }
    }

    public void refresh() {
        show();
    }
}
