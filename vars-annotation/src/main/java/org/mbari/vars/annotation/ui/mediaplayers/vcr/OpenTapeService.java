package org.mbari.vars.annotation.ui.mediaplayers.vcr;

import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.events.MediaChangedEvent;
import org.mbari.vars.vampiresquid.sdk.r1.MediaService;
import org.mbari.vars.annotation.etc.jdk.Loggers;

import java.time.Instant;

/**
 * @author Brian Schlining
 * @since 2018-03-27T13:16:00
 */
public class OpenTapeService {

    private final UIToolBox toolBox;
    private Loggers log = new Loggers(getClass());

    public OpenTapeService(UIToolBox toolBox) {
        this.toolBox = toolBox;
    }

    public void open(MediaParams mediaParams) {
        MediaService mediaService = toolBox.getServices().mediaService();
        if (mediaParams.getSerialPort() != null) {
            // Save serial port to preferences, so MediaControlsFactoryImplOriginal can look it up later
//            MediaControlsFactoryImpl.setSelectedSerialPort(mediaParams.getSerialPort());
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
                            log.atDebug().log(() -> "Created media: " + m);
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
