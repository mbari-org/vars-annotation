package org.mbari.m3.vars.annotation;

import io.reactivex.Observable;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.mbari.m3.vars.annotation.mediaplayers.MediaPlayer;
import org.mbari.m3.vars.annotation.mediaplayers.MediaPlayers;
import org.mbari.m3.vars.annotation.messages.*;
import org.mbari.m3.vars.annotation.events.*;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.services.CachedConceptService;
import org.mbari.m3.vars.annotation.services.ConceptService;
import org.mbari.m3.vars.annotation.ui.AnnotationServiceDecorator;
import org.mbari.m3.vars.annotation.ui.AppPaneController;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoState;
import org.mbari.vcr4j.time.Timecode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import java.time.Duration;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-05-10T09:55:00
 */
public class AppController {
    private Scene scene;
    private final UIToolBox toolBox;

    // Should automatically open the correct player. Listens for MediaChangedEvents
    private final MediaPlayers mediaPlayers;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public AppController(UIToolBox toolBox) {
        this.toolBox = toolBox;
        mediaPlayers = new MediaPlayers(toolBox);
        initialize();
    }

    public Scene getScene() {
        if (scene == null) {
            //AppPaneController paneController = new AppPaneController(toolBox);
            AppPaneController paneController = new AppPaneController(toolBox);
            scene = new Scene(paneController.getRoot());
            scene.getStylesheets()
                    .addAll(toolBox.getStylesheets());

            // --- Configure global shortcuts
            scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                KeyCode code = e.getCode();
                if (e.isControlDown()) {
                    if (code == KeyCode.SPACE) {
                        MediaPlayer<? extends VideoState, ? extends VideoError> mediaPlayer = toolBox.getMediaPlayer();
                        if (mediaPlayer != null) {
                            mediaPlayer.requestIsPlaying()
                                    .thenAccept(playing -> {
                                        if (playing) {
                                            mediaPlayer.stop();
                                        }
                                        else {
                                            mediaPlayer.play();
                                        }
                                    });
                        }
                    }
                }

                if (e.isMetaDown()) {
                    if (code == KeyCode.DOWN) {
                        TableView.TableViewSelectionModel<Annotation> selectionModel = paneController.getAnnotationTableController()
                                .getTableView()
                                .getSelectionModel();

                        int idx = selectionModel.getSelectedIndex();
                        selectionModel.clearSelection();
                        selectionModel.select(idx + 1);
                    }
                    else if (code == KeyCode.UP) {
                        TableView.TableViewSelectionModel<Annotation> selectionModel = paneController.getAnnotationTableController()
                                .getTableView()
                                .getSelectionModel();

                        int idx = selectionModel.getSelectedIndex();
                        selectionModel.clearSelection();
                        selectionModel.select(idx - 1);
                    }
                    else if (code == KeyCode.N) {
                        toolBox.getEventBus().send(new NewAnnotationMsg());
                    }
                    else if (code == KeyCode.C) {
                        toolBox.getEventBus().send(new CopyAnnotationMsg());
                    }
                    else if (code == KeyCode.T) {
                        toolBox.getEventBus().send(new DuplicateAnnotationMsg());
                    }
                    else if (code == KeyCode.F) {
                        toolBox.getEventBus().send(new FramecaptureMsg());
                    }
                }
            });
        }
        return scene;

    }

    private void initialize() {
        // wire up data to listen to events
        EventBus eventBus = toolBox.getEventBus();
        Data data = toolBox.getData();
        Observable<Object> eventObservable = eventBus.toObserverable();
        eventObservable.ofType(AnnotationsAddedEvent.class)
                .subscribe(e -> {
                            if (e.get() != null) {
                                data.getAnnotations().addAll(e.get());
                            }
                        },
                        er -> log.error("Subscriber failed", er));

        eventObservable.ofType(AnnotationsRemovedEvent.class)
                .subscribe(e -> {
                    // Remove from both annotations and selectedAnnotations.
                    // Reset selected to exclude any that were removed.
                    ArrayList<Annotation> selected = new ArrayList<>(data.getSelectedAnnotations());
                    selected.removeAll(e.get());
                    eventBus.send(new AnnotationsSelectedEvent(selected));
                    data.getAnnotations().removeAll(e.get());
                });

        eventObservable.ofType(AnnotationsChangedEvent.class)
                .subscribe(e -> {
                    // They use observation UUID as hash key. Remove and replace with new ones
                    data.getAnnotations().removeAll(e.get());
                    data.getAnnotations().addAll(e.get());
                });

        eventObservable.ofType(AnnotationsSelectedEvent.class)
                .subscribe(e -> data.setSelectedAnnotations(e.get()));

        eventObservable.ofType(MediaChangedEvent.class)
                .subscribe(e -> changeMedia(e.get()));

        eventObservable.ofType(UserChangedEvent.class)
                .subscribe(e -> data.setUser(e.get()));

        eventObservable.ofType(ClearCacheMsg.class)
                .subscribe(e -> {
                    ConceptService conceptService = toolBox.getServices().getConceptService();
                    if (conceptService instanceof CachedConceptService) {
                        ((CachedConceptService) conceptService).clear();
                    }
                });

        eventObservable.ofType(MediaPlayerChangedEvent.class)
                .subscribe(e -> toolBox.mediaPlayerProperty().set(e.get()));

        eventObservable.ofType(ShowConcurrentAnnotationsMsg.class)
                .subscribe(e -> showConcurrentMedia(e.getShow()));

        eventObservable.ofType(SeekMsg.class)
                .subscribe(this::seek);

    }

    private void seek(SeekMsg msg) {
        Object idx = msg.getIndex();
        MediaPlayer<? extends VideoState, ? extends VideoError> mediaPlayer = toolBox.getMediaPlayer();
        if (idx instanceof Timecode) {
            mediaPlayer.seek((Timecode) idx);
        }
        else if (idx instanceof Duration) {
            mediaPlayer.seek((Duration) idx);
        }
    }

    private void changeMedia(Media newMedia) {
        EventBus eventBus = toolBox.getEventBus();
        Data data = toolBox.getData();

        // Clear out old data
        eventBus.send(new AnnotationsSelectedEvent(new ArrayList<>()));
        eventBus.send(new AnnotationsRemovedEvent(data.getAnnotations()));
        eventBus.send(new ClearCommandManagerMsg());

        // Load new data
        data.setMedia(newMedia);
        AnnotationServiceDecorator decorator = new AnnotationServiceDecorator(toolBox);
        decorator.findAnnotations(newMedia.getVideoReferenceUuid());

    }

    private void showConcurrentMedia(Boolean show) {
        AnnotationServiceDecorator decorator = new AnnotationServiceDecorator(toolBox);
        Media media = toolBox.getData().getMedia();
        if (show) {
            if (media != null) {
                UUID uuid = media.getVideoReferenceUuid();

                toolBox.getServices()
                        .getMediaService()
                        .findConcurrentByVideoReferenceUuid(uuid)
                        .thenApply(ms -> ms.stream()
                                    .filter(m -> !m.getVideoReferenceUuid().equals(uuid))
                                    .map(Media::getVideoReferenceUuid)
                                    .collect(Collectors.toList()))
                        .thenAccept(decorator::findConcurrentAnnotations);
            }
        }
        else {
            if (media != null) {
                decorator.removeAnnotationsExceptFor(media.getVideoReferenceUuid());
            }
            else {
                ObservableList<Annotation> annotations = toolBox.getData().getAnnotations();
                EventBus eventBus = toolBox.getEventBus();
                eventBus.send(new AnnotationsSelectedEvent(new ArrayList<>()));
                eventBus.send(new AnnotationsRemovedEvent(annotations));
            }

        }
    }
}
