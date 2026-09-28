package org.mbari.vars.annotation.ui.mediaplayers;

import io.reactivex.rxjava3.schedulers.Schedulers;
import org.mbari.vars.annotation.etc.rxjava.EventBus;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.events.MediaChangedEvent;
import org.mbari.vars.annotation.ui.events.MediaControlsChangedEvent;
import org.mbari.vars.annotation.ui.events.MediaPlayerChangedEvent;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;

import org.mbari.vars.annotation.etc.jdk.Loggers;
import org.mbari.vars.annotation.etc.jdk.Streams;

import java.util.*;


/**
 * @author Brian Schlining
 * @since 2017-08-07T10:50:00
 */
public class MediaPlayers {

    private final UIToolBox toolBox;
    private final Loggers log = new Loggers(getClass());
    private final List<MediaControlsFactory> factories;
    private final Object lock = new byte[]{};

    public MediaPlayers(UIToolBox toolBox) {
        this(toolBox, loadFactories());
    }

    public MediaPlayers(UIToolBox toolBox, List<MediaControlsFactory> factories) {
        this.toolBox = toolBox;
        this.factories = factories;
        EventBus eventBus = toolBox.getEventBus();
        // MediaChangedEvent is often sent from the JavaFX application thread, but
        // opening a media blocks on UDP round trips to the video player. Hop to a
        // background thread so the UI stays responsive. observeOn delivers events
        // in order on a single worker, so opens don't interleave.
        eventBus.toObserverable()
                .ofType(MediaChangedEvent.class)
                .observeOn(Schedulers.from(toolBox.getExecutorService()))
                .subscribe(e -> open(e.get()));

        // Convert MediaControlsChangedEvent to MediaPlayerChangedEvent so that I don't have
        // to change all prexisting usages
        eventBus.toObserverable()
                .ofType(MediaControlsChangedEvent.class)
                .subscribe(e -> {
                    eventBus.send(new MediaPlayerChangedEvent(MediaPlayers.this,
                                    e.get().getMediaPlayer()));
                });
        Runtime.getRuntime()
                .addShutdownHook(new Thread(this::close));
    }

    private static List<MediaControlsFactory> loadFactories() {
        var serviceLoader = ServiceLoader.load(MediaControlsFactory.class,
                Thread.currentThread().getContextClassLoader());
        return Streams.toStream(serviceLoader.iterator()).toList();
    }

    public List<SettingsPane> getSettingsPanes() {
        return factories.stream()
                .map(MediaControlsFactory::getSettingsPane)
                .filter(Objects::nonNull)
                .toList();
    }

    private void open(Media media) {

        // Close the old one
        synchronized (lock) {
            close();
            var eventBus = toolBox.getEventBus();

            try {
                factories.stream()
                        .filter(factory -> factory.canOpen(media))
                        .peek(factory -> log.atDebug().log(() -> "Opening " + media.getUri() + " using factory: " + factory))
                        .findFirst()
                        .ifPresent(factory -> {
                            try {
                                var mediaControls = factory.safeOpen(media);
                                eventBus.send(new MediaControlsChangedEvent(MediaPlayers.this,
                                                mediaControls));
                            } catch (Exception e) {
                                log.atError().withCause(e).log("Unable to load services");
                                eventBus.send(new MediaPlayerChangedEvent(null, null));
                            }
                        });

            } catch (ServiceConfigurationError e) {
                log.atError().withCause(e).log("Unable to load services");
                eventBus.send(new MediaPlayerChangedEvent(null, null));
            }
        }
    }


    private void close() {
        // Close the old MediaPlayer
        log.atDebug().log(() -> "Closing MediaPlayer: " + toolBox.getMediaPlayer());
        Optional.ofNullable(toolBox.getMediaPlayer())
                .ifPresent(MediaPlayer::close);
    }
}
