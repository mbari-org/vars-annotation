package org.mbari.vars.annotation.ui.mediaplayers.macos.bm;

import org.mbari.vars.vampiresquid.sdk.r1.models.Media;
import org.mbari.vars.annotation.ui.mediaplayers.MediaControls;
import org.mbari.vars.annotation.ui.mediaplayers.MediaControlsFactory;
import org.mbari.vars.annotation.ui.mediaplayers.SettingsPane;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class MediaControlsFactoryImpl implements MediaControlsFactory {

    private SettingsPane settingsPane;
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public SettingsPane getSettingsPane() {
        if (settingsPane == null) {
            try {
                settingsPane = BMSettingsPaneController.newInstance();
            }
            catch (Exception | UnsatisfiedLinkError e) {
                log.warn("Unable to create a settings pane for Blackmagic Server image capture", e);
            }
        }
        return settingsPane;
    }

    /**
     * This implementation never directly opens any media.
     *
     * @param media The media we want to open
     * @return
     */
    @Override
    public boolean canOpen(Media media) {
        return false;
    }

    @Override
    public CompletableFuture<MediaControls<? extends VideoState, ? extends VideoError>> open(Media media) {
        CompletableFuture<MediaControls<? extends VideoState, ? extends VideoError>> cf =
                new CompletableFuture<>();
        cf.completeExceptionally(new RuntimeException("This implementation can not open media directly!"));
        return cf;
    }
}
