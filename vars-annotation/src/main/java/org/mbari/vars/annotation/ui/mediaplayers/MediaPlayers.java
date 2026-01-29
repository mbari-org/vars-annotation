package org.mbari.vars.annotation.ui.mediaplayers;

import org.mbari.vars.annotation.etc.rxjava.EventBus;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.events.ForceReloadLocalizationsEvent;
import org.mbari.vars.annotation.ui.events.MediaChangedEvent;
import org.mbari.vars.annotation.ui.events.MediaControlsChangedEvent;
import org.mbari.vars.annotation.ui.events.MediaPlayerChangedEvent;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;

import mbarix4j.util.stream.StreamUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author Brian Schlining
 * @since 2017-08-07T10:50:00
 */
public class MediaPlayers {

    private final UIToolBox toolBox;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private ServiceLoader<MediaControlsFactory> serviceLoader;
    private final Object lock = new byte[]{};

    public MediaPlayers(UIToolBox toolBox) {
        this.toolBox = toolBox;
        serviceLoader = ServiceLoader.load(MediaControlsFactory.class);
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
        return StreamUtilities.toStream(serviceLoader.iterator())
                .map(MediaControlsFactory::getSettingsPane)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void open(Media media) {

        // Close the old one
        synchronized (lock) {
            close();
            var eventBus = toolBox.getEventBus();

            try {
                StreamUtilities.toStream(serviceLoader.iterator())
                        .peek(factory -> log.debug("ServiceLoader found a factory: {}", factory))
                        .filter(factory -> factory.canOpen(media))
                        .peek(factory -> log.debug("ServiceLoader using a factory: {}", factory))
                        .findFirst()
                        .ifPresent(factory -> {
                            try {
                                var mediaControls = factory.safeOpen(media);
                                eventBus.send(new MediaControlsChangedEvent(MediaPlayers.this,
                                                mediaControls));
                            } catch (Exception e) {
                                log.error("Unable to load services", e);
                                eventBus.send(new MediaPlayerChangedEvent(null, null));
                            }
                            // #174: Annotations are sometimes send before the media
                            // is ready. So this triggers a clear and reload in the
                            // OutgoingController
                            eventBus.send(new ForceReloadLocalizationsEvent());
                        });

            } catch (ServiceConfigurationError e) {
                log.error("Unable to load services", e);
                eventBus.send(new MediaPlayerChangedEvent(null, null));
            }
        }
    }


    private void close() {
        // Close the old MediaPlayer
        log.debug("Closing MediaPlayer: " + toolBox.getMediaPlayer());
        Optional.ofNullable(toolBox.getMediaPlayer())
                .ifPresent(MediaPlayer::close);
    }
}
