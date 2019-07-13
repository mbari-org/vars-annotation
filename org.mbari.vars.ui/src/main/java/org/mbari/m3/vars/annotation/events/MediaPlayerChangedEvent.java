package org.mbari.m3.vars.annotation.events;

import org.mbari.m3.vars.annotation.mediaplayers.MediaPlayer;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoState;

/**
 * @author Brian Schlining
 * @since 2017-08-10T15:51:00
 */
public class MediaPlayerChangedEvent
        extends UIChangeEvent<MediaPlayer<? extends VideoState, ? extends VideoError>>{

    public MediaPlayerChangedEvent(Object changeSource, MediaPlayer<? extends VideoState, ? extends VideoError> refs) {
        super(changeSource, refs);
    }
}
