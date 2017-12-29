package org.mbari.m3.vars.annotation.mediaplayers;

import javafx.scene.layout.Pane;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoState;

/**
 * @author Brian Schlining
 * @since 2017-12-28T12:44:00
 */
public class MediaControls<S extends VideoState, E extends VideoError> {

    private final MediaPlayer<S, E> mediaPlayer;
    private final Pane mediaControlPanel;

    public MediaControls(MediaPlayer<S, E> mediaPlayer, Pane mediaControlPanel) {
        this.mediaPlayer = mediaPlayer;
        this.mediaControlPanel = mediaControlPanel;
    }

    public MediaPlayer<S, E> getMediaPlayer() {
        return mediaPlayer;
    }

    public Pane getMediaControlPanel() {
        return mediaControlPanel;
    }
}
