package org.mbari.m3.vars.annotation.mediaplayers.ships;

import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.events.MediaChangedEvent;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.services.MediaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * @author Brian Schlining
 * @since 2018-01-03T11:25:00
 */
public class OpenRealTimeService {

    private final UIToolBox toolBox;
    private Logger log = LoggerFactory.getLogger(getClass());

    public OpenRealTimeService(UIToolBox toolBox) {
        this.toolBox = toolBox;
    }

    public void open(MediaParams mediaParams) {
        MediaService mediaService = toolBox.getServices().getMediaService();
        // Find existing media by URI
        mediaService.findByUri(mediaParams.getUri())
                .thenAccept(media -> {
                    log.debug("Media lookup returned: " + media);
                    if (media == null) {
                        // Create one
                        mediaService.create(mediaParams.getVideoSequenceName(),
                                mediaParams.getCameraId(),
                                mediaParams.getVideoName(),
                                mediaParams.getUri(),
                                mediaParams.getStartTimestamp())
                            .thenAccept(m -> {
                                log.debug("Created media: " + m);

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
