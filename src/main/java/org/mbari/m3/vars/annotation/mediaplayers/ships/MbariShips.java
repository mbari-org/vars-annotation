package org.mbari.m3.vars.annotation.mediaplayers.ships;

import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.mediaplayers.MediaPlayer;
import org.mbari.m3.vars.annotation.mediaplayers.NoopImageCaptureService;
import org.mbari.m3.vars.annotation.mediaplayers.macos.AVFImageCaptureService;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.services.ImageCaptureService;
import org.mbari.util.SystemUtilities;
import org.mbari.vars.avfoundation.AVFImageCapture;
import org.mbari.vcr4j.SimpleVideoError;
import org.mbari.vcr4j.VideoIO;
import org.mbari.vcr4j.decorators.SchedulerVideoIO;
import org.mbari.vcr4j.sharktopoda.SharktopodaError;
import org.mbari.vcr4j.sharktopoda.SharktopodaState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

/**
 * @author Brian Schlining
 * @since 2017-12-20T15:44:00
 */
public class MbariShips {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private AVFImageCaptureService imageCaptureService;
    private final UIToolBox toolBox;

    @Inject
    public MbariShips(UIToolBox toolBox) {
        this.toolBox = toolBox;
        if (SystemUtilities.isMacOS()) {
            imageCaptureService = new AVFImageCaptureService();
        }
    }

    public CompletableFuture<MediaPlayer<ShipVideoState, SimpleVideoError>> open(Media media, String captureDevice) {

        CompletableFuture<MediaPlayer<ShipVideoState, SimpleVideoError>> cf = new CompletableFuture<>();

        ImageCaptureService ics = imageCaptureService == null ? new NoopImageCaptureService() :
                imageCaptureService;

        try {
            ShipVideoIO videoIO = new ShipVideoIO("Real-time for " + media.getCameraId());
            VideoIO<ShipVideoState, SimpleVideoError> io =
                    new SchedulerVideoIO<>(videoIO, Executors.newCachedThreadPool());
            MediaPlayer<ShipVideoState, SimpleVideoError> newVc =
                    new MediaPlayer<>(media, ics, io,
                            () -> {
                        // TODO get date of first and last annotation and
                                // update the media start_timestamp and duration
                            });
        }
        catch (Exception e) {
            // TODO handle exception
        }
    }


}
