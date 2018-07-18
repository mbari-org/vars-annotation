package org.mbari.m3.vars.annotation;

import io.reactivex.Observable;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import org.mbari.io.IOUtilities;
import org.mbari.m3.vars.annotation.mediaplayers.MediaPlayer;
import org.mbari.m3.vars.annotation.mediaplayers.MediaPlayers;
import org.mbari.m3.vars.annotation.messages.*;
import org.mbari.m3.vars.annotation.events.*;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.services.CachedConceptService;
import org.mbari.m3.vars.annotation.services.ConceptService;
import org.mbari.m3.vars.annotation.ui.Alerts;
import org.mbari.m3.vars.annotation.ui.AnnotationServiceDecorator;
import org.mbari.m3.vars.annotation.ui.AppPaneController;
import org.mbari.net.URLUtilities;
import org.mbari.util.SystemUtilities;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoState;
import org.mbari.vcr4j.time.Timecode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-05-10T09:55:00
 */
public class AppController {
    private Scene scene;
    private final UIToolBox toolBox;
    private final Alerts alerts;

    // Should automatically open the correct player. Listens for MediaChangedEvents
    private final MediaPlayers mediaPlayers;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final FileChooser fileChooser = new FileChooser();

    public AppController(UIToolBox toolBox) {
        this.toolBox = toolBox;
        alerts = new Alerts(toolBox);
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

            KeyCombination.Modifier osModifier = SystemUtilities.isMacOS() ?
                    KeyCombination.META_DOWN : KeyCombination.CONTROL_DOWN;

            KeyCodeCombination spaceCombo = new KeyCodeCombination(KeyCode.SPACE, KeyCombination.CONTROL_DOWN);
            KeyCodeCombination downCombo = new KeyCodeCombination(KeyCode.DOWN, osModifier);
            KeyCodeCombination upCombo = new KeyCodeCombination(KeyCode.UP, osModifier);
            KeyCodeCombination nCombo = new KeyCodeCombination(KeyCode.N, osModifier);
            KeyCodeCombination cCombo = new KeyCodeCombination(KeyCode.G, osModifier);
            KeyCodeCombination tCombo = new KeyCodeCombination(KeyCode.T, osModifier);
            KeyCodeCombination fCombo = new KeyCodeCombination(KeyCode.F, osModifier);
            KeyCodeCombination deleteCombo = new KeyCodeCombination(KeyCode.DELETE, osModifier);

            // --- Configure global shortcuts
            scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {

                if (spaceCombo.match(e)) {
                    MediaPlayer<? extends VideoState, ? extends VideoError> mediaPlayer = toolBox.getMediaPlayer();
                    if (mediaPlayer != null) {
                        mediaPlayer.requestIsPlaying()
                                .thenAccept(playing -> {
                                    if (playing) {
                                        mediaPlayer.stop();
                                    } else {
                                        mediaPlayer.play();
                                    }
                                });
                    }
                    e.consume();
                }
                else if (downCombo.match(e)) {
                    TableView.TableViewSelectionModel<Annotation> selectionModel = paneController.getAnnotationTableController()
                            .getTableView()
                            .getSelectionModel();

                    int idx = selectionModel.getSelectedIndex();
                    selectionModel.clearSelection();
                    selectionModel.select(idx + 1);
                    e.consume();
                }
                else if (upCombo.match(e)) {
                    TableView.TableViewSelectionModel<Annotation> selectionModel = paneController.getAnnotationTableController()
                            .getTableView()
                            .getSelectionModel();

                    int idx = selectionModel.getSelectedIndex();
                    selectionModel.clearSelection();
                    selectionModel.select(idx - 1);
                    e.consume();
                }
                else if (nCombo.match(e)) {
                    toolBox.getEventBus().send(new NewAnnotationMsg());
                    e.consume();
                }
                else if(cCombo.match(e)) {
                    toolBox.getEventBus().send(new CopyAnnotationMsg());
                    e.consume();
                }
                else if (tCombo.match(e)) {
                    toolBox.getEventBus().send(new DuplicateAnnotationMsg());
                    e.consume();
                }
                else if (fCombo.match(e)) {
                    toolBox.getEventBus().send(new FramecaptureMsg());
                    e.consume();
                }
                else if (deleteCombo.match(e)) {
                    toolBox.getEventBus().send(new DeleteAnnotationsMsg());
                    e.consume();
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

        eventObservable.ofType(SaveImageMsg.class)
                .subscribe(this::saveImage);

        eventObservable.ofType(ShowAlert.class)
                .subscribe(alerts::showAlert);

    }

    private void saveImage(SaveImageMsg msg) {
        URL url = msg.getUrl();
        if (url != null ) {
            String filename = URLUtilities.toFilename(url);
            fileChooser.setTitle(toolBox.getI18nBundle().getString("appcontroller.imagesave.title"));
            fileChooser.setInitialFileName(filename);
            File file = fileChooser.showSaveDialog(msg.getWindow());

            if (file != null) {
                try (BufferedInputStream in = new BufferedInputStream(url.openStream());
                     BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
                    IOUtilities.copy(in, out);
                }
                catch (IOException e) {
                    log.error("Failed to copy " + url + " to " + file);
                }
            }
        }
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
        else if (idx instanceof Instant) {
            Media media = toolBox.getData().getMedia();
            if (media != null) {
                Instant startTime = media.getStartTimestamp();
                if (startTime != null) {
                    Instant time = (Instant) idx;
                    Duration elapsedTime = Duration.between(startTime, time);
                    mediaPlayer.seek(elapsedTime);
                }
            }
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
        if (newMedia != null) {
            decorator.findAnnotations(newMedia.getVideoReferenceUuid());
        }


    }

    private void showConcurrentMedia(Boolean show) {
        AnnotationServiceDecorator decorator = new AnnotationServiceDecorator(toolBox);
        Media media = toolBox.getData().getMedia();
        if (show) {
            if (media != null) {
                UUID uuid = media.getVideoReferenceUuid();

                /*
                  1. Find medias for your deployment that overlap the one with
                     the uuid you provided
                  2. Convert those medias to a list of their UUIDs
                  3. Pass that list to findConcurrentAnnotations. That will
                     get all annotations, from the overlapping media, that
                     overlap with the timebounds of your current media
                 */
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
