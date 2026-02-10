package org.mbari.vars.annotation.ui.mediaplayers.macos.avf;

import org.mbari.vars.annotation.etc.jdk.Loggers;
import org.mbari.vars.annotation.ui.mediaplayers.MediaControls;
import org.mbari.vars.annotation.ui.mediaplayers.MediaControlsFactory;
import org.mbari.vars.annotation.ui.mediaplayers.SettingsPane;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;
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
    private final Loggers log = new Loggers(getClass());

    @Override
    public SettingsPane getSettingsPane() {
        if (settingsPane == null) {
            try {
                settingsPane = MacImageCaptureSettingsPaneController.newInstance();
            }
            catch (Exception | UnsatisfiedLinkError e) {
                log.atWarn().withCause(e).log("Unable to create a settings pane for Mac OS Image capture");
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
