package org.mbari.m3.vars.annotation.mediaplayers.vcr;

import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.events.MediaChangedEvent;
import org.mbari.m3.vars.annotation.services.MediaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * @author Brian Schlining
 * @since 2018-03-27T13:16:00
 */
public class OpenTapeService {

    private final UIToolBox toolBox;
    private Logger log = LoggerFactory.getLogger(getClass());

    public OpenTapeService(UIToolBox toolBox) {
        this.toolBox = toolBox;
    }

    public void open(MediaParams mediaParams) {
        MediaService mediaService = toolBox.getServices().getMediaService();
        if (mediaParams.getSerialPort() != null) {
            // Save serial port to preferences, so MediaControlsFactoryImplOriginal can look it up later
            MediaControlsFactoryImplOriginal.setSelectedSerialPort(mediaParams.getSerialPort());
        }
        mediaService.findByUri(mediaParams.getUri())
                .thenAccept(media -> {
                    if (media == null) {
                        mediaService.create(mediaParams.getVideoSequenceName(),
                                mediaParams.getCameraId(),
                                mediaParams.getVideoName(),
                                mediaParams.getUri(),
                                Instant.now()) // use now as placeholder time.
                                // THis is changed when media is closed at end of annotation session
                        .thenAccept(m -> {
                            log.debug("Created media: " + m);
                            toolBox.getEventBus()
                                    .send(new MediaChangedEvent(null, m));
                        });
                    }
                    else {
                        toolBox.getEventBus()
                                .send(new MediaChangedEvent(null, media));
                    }
                });
    }
}
