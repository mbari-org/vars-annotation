package org.mbari.m3.vars.annotation.mediaplayers.macos;

import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.mediaplayers.MediaControls;
import org.mbari.m3.vars.annotation.mediaplayers.MediaControlsFactory;
import org.mbari.m3.vars.annotation.mediaplayers.SettingsPane;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoState;

import java.util.concurrent.CompletableFuture;

/**
 * This is a do-nothing implementation. It's used to dynamically load the preference pane
 * into the settings dialog
 *
 * @author Brian Schlining
 * @since 2017-12-29T10:37:00
 */
public class MediaControlsFactoryImpl implements MediaControlsFactory {

    private SettingsPane settingsPane;

    @Override
    public SettingsPane getSettingsPane() {
        if (settingsPane == null) {
            settingsPane = new SettingsPaneImpl(Initializer.getToolBox(),
                    AVFImageCaptureService.getInstance());
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
