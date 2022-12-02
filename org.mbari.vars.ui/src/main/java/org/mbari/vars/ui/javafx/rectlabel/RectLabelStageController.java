package org.mbari.vars.ui.javafx.rectlabel;

import io.reactivex.rxjava3.disposables.Disposable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.mbari.vars.core.EventBus;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.commands.Command;
import org.mbari.vars.ui.commands.DeleteAssociationsCmd;
import org.mbari.vars.ui.events.AnnotationsAddedEvent;
import org.mbari.vars.ui.events.MediaChangedEvent;
import org.mbari.vars.services.model.Association;
import org.mbari.vars.services.model.Media;
import org.mbari.vars.services.AnnotationService;
import org.mbari.vars.ui.util.JFXUtilities;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

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
    private boolean visible = false;


    public RectLabelStageController(UIToolBox toolBox) {
        this.toolBox = toolBox;
        rectLabelController = RectLabelController.newInstance(toolBox);
        rectLabelController.getRefreshButton()
                .setOnAction(evt -> show());
        EventBus eventBus = toolBox.getEventBus();
        rectLabelController.getDeleteButton()
                .setOnAction(evt -> {
                    Map<Association, UUID> map = rectLabelController.getSelectedBoundingBoxAssociations();
                    if (!map.isEmpty()) {
                        Command cmd = new DeleteAssociationsCmd(map);
                        eventBus.send(cmd);
                    }
                });

//        eventBus.toObserverable()
//                .ofType(AnnotationsSelectedEvent.class)
//                .filter(evt -> evt.getEventSource() != rectLabelController)
//                .subscribe(rectLabelController::handleAnnotationsSelectedEvent);

        eventBus.toObserverable()
                .ofType(AnnotationsAddedEvent.class)
                .filter(evt -> evt.getEventSource() != rectLabelController)
                .subscribe(evt -> {
                    if (visible) {
                        show();
                    }
                });

    }

    /**
     * Sets the window visible and sets the media to the currently selected media
     * used in the main annotation window
     */
    public void show() {
        visible = true;
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

    public Stage getStage() {
        if (stage == null) {
            stage = new Stage();
            BorderPane root = rectLabelController.getRoot();
            Scene scene = new Scene(root);
            scene.getStylesheets().addAll(toolBox.getStylesheets());
            stage.setScene(scene);
        }
        return stage;
    }

    /**
     * Hides the stage and disables eventbus and redraws
     */
    public void hide() {
        visible = false;
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
