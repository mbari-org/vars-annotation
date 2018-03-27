package org.mbari.m3.vars.annotation.mediaplayers.vcr;

import org.mbari.m3.vars.annotation.mediaplayers.MediaControls;
import org.mbari.m3.vars.annotation.mediaplayers.MediaControlsFactory;
import org.mbari.m3.vars.annotation.mediaplayers.NoopImageCaptureService;
import org.mbari.m3.vars.annotation.mediaplayers.SettingsPane;
import org.mbari.m3.vars.annotation.mediaplayers.macos.MacImageCaptureServiceRef;
import org.mbari.m3.vars.annotation.mediaplayers.ships.MediaParams;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.services.ImageCaptureService;
import org.mbari.util.SystemUtilities;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoState;
import org.mbari.vcr4j.rxtx.RXTX;

import java.util.concurrent.CompletableFuture;

/**
 * @author Brian Schlining
 * @since 2018-03-26T10:52:00
 */
public class MediaControlsFactoryImpl implements MediaControlsFactory {


    @Override
    public SettingsPane getSettingsPane() {
        return null;
    }

    @Override
    public boolean canOpen(Media media) {
        return media != null &&
                media.getUri() != null &&
                media.getUri().toString().startsWith(MediaParams.URI_PREFIX);
    }

    @Override
    public CompletableFuture<MediaControls<? extends VideoState, ? extends VideoError>> open(Media media) {
        CompletableFuture<MediaControls<? extends VideoState, ? extends VideoError>> cf =
                new CompletableFuture<>();

        ImageCaptureService imageCaptureService = new NoopImageCaptureService();
        if (SystemUtilities.isMacOS()) {
            imageCaptureService = new MacImageCaptureServiceRef();
        }

        RXTX.setup();




        return cf;
    }
}
