package org.mbari.vars.annotation.ui.mediaplayers;

import org.mbari.vars.annotation.etc.rxjava.EventBus;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.events.ForceReloadLocalizationsEvent;
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
    private final ServiceLoader<MediaControlsFactory> serviceLoader;
    private final Object lock = new byte[]{};

    public MediaPlayers(UIToolBox toolBox) {
        this.toolBox = toolBox;
        serviceLoader = ServiceLoader.load(MediaControlsFactory.class, Thread.currentThread().getContextClassLoader());
        EventBus eventBus = toolBox.getEventBus();
        eventBus.toObserverable()
                .ofType(MediaChangedEvent.class)
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

    public List<SettingsPane> getSettingsPanes() {
        log.atWarn().log("Loading MediaControlsFactories");
//        for (var factory : serviceLoader) {
//            log.warn("Discovered MediaControlsFactory: {}", factory.getClass().getName());
//        }
        return Streams.toStream(serviceLoader.iterator())
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
                Streams.toStream(serviceLoader.iterator())
                        .peek(factory -> log.atDebug().log(() -> "ServiceLoader found a factory: " + factory))
                        .filter(factory -> factory.canOpen(media))
                        .peek(factory -> log.atDebug().log(() -> "ServiceLoader using a factory: " + factory))
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
                            // #174: Annotations are sometimes sent before the media
                            // is ready. So this triggers a clear and reload in the
                            // OutgoingController
                            eventBus.send(new ForceReloadLocalizationsEvent());
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
