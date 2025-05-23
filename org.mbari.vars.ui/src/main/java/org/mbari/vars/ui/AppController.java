package org.mbari.vars.ui;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import org.mbari.vars.core.EventBus;
import org.mbari.vars.services.CachedMediaService;
import org.mbari.vars.services.MediaService;
import org.mbari.vars.services.impl.varsuserserver.v1.CachedKBPrefService;
import org.mbari.vars.ui.mediaplayers.MediaPlayer;
import org.mbari.vars.ui.mediaplayers.MediaPlayers;
import org.mbari.vars.ui.messages.*;
import org.mbari.vars.ui.events.*;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Media;
import org.mbari.vars.services.CachedConceptService;
import org.mbari.vars.ui.javafx.Alerts;
import org.mbari.vars.ui.javafx.AnnotationServiceDecorator;
import org.mbari.vars.ui.services.ConcurrentAnnotationDecorator;
import org.mbari.vars.ui.javafx.AppPaneController;
import org.mbari.vars.oni.sdk.r1.ConceptService;

import mbarix4j.io.IOUtilities;
import mbarix4j.net.URLUtilities;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoState;
import org.mbari.vcr4j.time.Timecode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * This is the main controller for the App
 *
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
    // Disabled ZeroMQ for https://github.com/mbari-media-management/vars-annotation/issues/148
//    private final LocalizationLifecycleController localizationLifecycleController;

    public AppController(UIToolBox toolBox) {
        this.toolBox = toolBox;
        alerts = new Alerts(toolBox);
        mediaPlayers = new MediaPlayers(toolBox);
//        localizationLifecycleController = new LocalizationLifecycleController(toolBox);
        initialize();
    }

    public Scene getScene() {
        if (scene == null) {
            AppPaneController paneController = new AppPaneController(toolBox);
            scene = new Scene(paneController.getRoot());
            scene.getStylesheets()
                    .addAll(toolBox.getStylesheets());
            new KeyMapping(toolBox, scene, paneController);
        }
        return scene;

    }

    private void initialize() {
        // wire up data to listen to events
        EventBus eventBus = toolBox.getEventBus();
        Data data = toolBox.getData();
        Observable<Object> eventObservable = eventBus.toObserverable();
        eventObservable.subscribe(e -> log.debug("There's an event on the EventBus: " + e.toString()));
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
                    log.info("Removing {} annotations from internal data cache", e.get().size() );
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

        eventObservable.ofType(ReloadServicesMsg.class)
                .subscribe(e -> {
                    // Clear KB cache
                    org.mbari.vars.oni.sdk.r1.ConceptService conceptService = toolBox.getServices().getConceptService();
                    if (conceptService instanceof CachedConceptService) {
                        ((CachedConceptService) conceptService).clear();
                    }

                    MediaService mediaService = toolBox.getServices().getMediaService();
                    if (mediaService instanceof CachedMediaService) {
                        ((CachedMediaService) mediaService).clear();
                    }

                    // Clear pref cache
                    var preferenceService = toolBox.getServices().getPreferencesService();
                    if (preferenceService instanceof CachedKBPrefService) {
                        ((CachedKBPrefService) preferenceService).clear();
                    }
                    // Close open media, reopen current media
                    // https://github.com/mbari-media-management/vars-annotation/issues/157
                    var lastOpenedMedia = toolBox.getData().getMedia();
                    eventBus.send(new MediaChangedEvent(AppController.this, null));
                    if (lastOpenedMedia != null) {
                        new Thread(() -> {
                            try {
                                eventBus.send(new ShowInfoAlert("VARS", "Reopening video", "Reopening " + lastOpenedMedia.getVideoName() + " after refresh"));
                                Thread.sleep(1500);
                                toolBox.getServices()
                                        .getMediaService()
                                        .findByUuid(lastOpenedMedia.getVideoReferenceUuid())
                                        .thenAccept(media -> {
                                            eventBus.send(new MediaChangedEvent(AppController.this, lastOpenedMedia));
                                        });

                            } catch (InterruptedException ex) {
                                log.atWarn()
                                        .setCause(ex)
                                        .log("An error occurred while waiting to reopen " + lastOpenedMedia);
                            }
                        }).start();
                    }
                });

        eventObservable.ofType(MediaPlayerChangedEvent.class)
                .subscribe(e -> toolBox.mediaPlayerProperty().set(e.get()));

        eventObservable.ofType(ShowConcurrentAnnotationsMsg.class)
                .subscribe(e -> showConcurrentMedia(e.show()));

        eventObservable.ofType(ShowJsonAssociationsMsg.class)
                .subscribe(e -> toolBox.getData().setShowJsonAssociations(e.show()));

        eventObservable.ofType(ShowCurrentGroupOnlyMsg.class)
                .subscribe(e -> toolBox.getData().setShowCurrentGroupOnly(e.show()));

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

    private void seek(SeekMsg<?> msg) {
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
            // Load annotations for media & if needed, show concurrent annotations
            decorator.findAnnotations(newMedia.getVideoReferenceUuid())
                .thenAccept(v ->
                        eventBus.send(new ShowConcurrentAnnotationsMsg(data.isShowConcurrentAnnotations())));
        }


    }

    private void showConcurrentMedia(Boolean show) {
        AnnotationServiceDecorator decorator = new AnnotationServiceDecorator(toolBox);
        Media media = toolBox.getData().getMedia();
        toolBox.getData().setShowConcurrentAnnotations(show);
        if (show) {
            if (media != null) {
                ConcurrentAnnotationDecorator d2 = new ConcurrentAnnotationDecorator(toolBox);
                d2.loadConcurrentAnnotations(media);
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
