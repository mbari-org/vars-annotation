package org.mbari.vars.annotation.ui.mediaplayers.ships;

import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.events.MediaChangedEvent;
import org.mbari.vars.vampiresquid.sdk.r1.MediaService;
import org.mbari.vars.annotation.etc.jdk.Loggers;

/**
 * @author Brian Schlining
 * @since 2018-01-03T11:25:00
 */
public class OpenRealTimeService {

    private final UIToolBox toolBox;
    private Loggers log = new Loggers(getClass());

    public OpenRealTimeService(UIToolBox toolBox) {
        this.toolBox = toolBox;
    }

    public void open(MediaParams mediaParams) {
        MediaService mediaService = toolBox.getServices().mediaService();
        // Find existing media by URI
        mediaService.findByUri(mediaParams.getUri())
                .thenAccept(media -> {
                    log.atDebug().log(() -> "Media lookup returned: " + media);
                    if (media == null) {
                        // Create one
                        mediaService.create(mediaParams.getVideoSequenceName(),
                                mediaParams.getCameraId(),
                                mediaParams.getVideoName(),
                                mediaParams.getUri(),
                                mediaParams.getStartTimestamp())
                            .thenAccept(m -> {
                                log.atDebug().log(() -> "Created media: " + m);

                                // TODO send error to event bus is media is null
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
