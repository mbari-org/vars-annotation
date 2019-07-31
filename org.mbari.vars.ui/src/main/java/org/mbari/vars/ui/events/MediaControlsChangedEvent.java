package org.mbari.vars.ui.events;

import org.mbari.vars.ui.mediaplayers.MediaControls;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoState;

/**
 * @author Brian Schlining
 * @since 2017-12-28T14:22:00
 */
public class MediaControlsChangedEvent
        extends UIChangeEvent<MediaControls<? extends VideoState, ? extends VideoError>> {

    public MediaControlsChangedEvent(Object changeSource, MediaControls<? extends VideoState, ? extends VideoError> refs) {
        super(changeSource, refs);
    }
}
