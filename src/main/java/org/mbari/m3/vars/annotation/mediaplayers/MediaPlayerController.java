package org.mbari.m3.vars.annotation.mediaplayers;

import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.events.MediaChangedEvent;

import java.net.URI;

/**
 * @author Brian Schlining
 * @since 2017-08-07T10:50:00
 */
public class MediaPlayerController {

    private final UIToolBox toolBox;

    public MediaPlayerController(UIToolBox toolBox) {
        this.toolBox = toolBox;
        toolBox.getEventBus()
                .toObserverable()
                .ofType(MediaChangedEvent.class)
                .subscribe(e -> open(e.get().getUri()));
    }

    private void open(URI uri) {}
}
