package org.mbari.m3.vars.annotation.mediaplayers.ships;

import org.mbari.m3.vars.annotation.mediaplayers.MediaControls;
import org.mbari.m3.vars.annotation.mediaplayers.MediaControlsFactory;
import org.mbari.m3.vars.annotation.mediaplayers.SettingsPane;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoState;

import java.util.concurrent.CompletableFuture;

/**
 * @author Brian Schlining
 * @since 2017-12-28T16:03:00
 */
public class MediaControlsFactoryImpl implements MediaControlsFactory {

    @Override
    public SettingsPane getSettingsPane() {
        return null;
    }

    @Override
    public boolean canOpen(Media media) {
        return false;
    }

    @Override
    public CompletableFuture<MediaControls<? extends VideoState, ? extends VideoError>> open(Media media) {
        return null;
    }
}
