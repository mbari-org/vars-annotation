package org.mbari.vars.ui.events;

import org.mbari.vars.services.model.Media;

/**
 * Event that signals a new Media has been selected for annotation.
 * @author Brian Schlining
 * @since 2017-07-20T17:07:00
 */
public class MediaChangedEvent extends UIChangeEvent<Media> {

    public MediaChangedEvent(Object changeSource, Media media) {
        super(changeSource, media);
    }
}
