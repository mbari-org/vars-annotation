package org.mbari.m3.vars.annotation.services.vcr4j;

import org.mbari.m3.vars.annotation.services.VideoControlService;
import org.mbari.vcr4j.VideoIO;
import org.mbari.vcr4j.decorators.SchedulerVideoIO;
import org.mbari.vcr4j.decorators.StatusDecorator;
import org.mbari.vcr4j.decorators.VCRSyncDecorator;
import org.mbari.vcr4j.sharktopoda.SharktopodaError;
import org.mbari.vcr4j.sharktopoda.SharktopodaState;
import org.mbari.vcr4j.sharktopoda.SharktopodaVideoIO;
import org.mbari.vcr4j.sharktopoda.commands.OpenCmd;
import org.mbari.vcr4j.sharktopoda.commands.SharkCommands;
import org.mbari.vcr4j.sharktopoda.decorators.FauxTimecodeDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Brian Schlining
 * @since 2017-07-20T16:37:00
 */
public class SharktopodaVideoPlayer {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final AtomicReference<VideoControlService<SharktopodaState, SharktopodaError>> currentVideoController = new AtomicReference<>();

    public CompletableFuture<VideoControlService<SharktopodaState, SharktopodaError>> createVideoController(String movieLocation,
                                                                                                           int sharktopodaPort,
                                                                                                           int framecapturePort) {
        CompletableFuture<VideoControlService<SharktopodaState, SharktopodaError>> cf = new CompletableFuture<>();

        try {
            SharktopodaVideoIO videoIO = new SharktopodaVideoIO(UUID.randomUUID(),
                    "localhost",
                    sharktopodaPort);
            videoIO.send(new OpenCmd(new URL(movieLocation)));
            new StatusDecorator<>(videoIO);
            new VCRSyncDecorator<>(videoIO, 1000, 100, 3000000);
            new FauxTimecodeDecorator(videoIO); // Convert elapsed-time to timecode
            VideoIO<SharktopodaState, SharktopodaError> io = new SchedulerVideoIO<>(videoIO, Executors.newCachedThreadPool());
            VideoControlService<SharktopodaState, SharktopodaError> oldVc = currentVideoController.get();
            if (oldVc != null) {
                oldVc.getVideoIO().send(SharkCommands.CLOSE);
                oldVc.getImageCaptureService().dispose();
            }
            VideoControlService<SharktopodaState, SharktopodaError> newVc =
                    new VideoControlService<>(
                            new SharktopodaImageCaptureService(videoIO, framecapturePort), io);
            currentVideoController.set(newVc);
            cf.complete(newVc);
            io.send(SharkCommands.SHOW);
        }
        catch (Exception e) {
            log.error("Failed to create SharktopodaVideoIO", e);
            cf.completeExceptionally(e);
        }

        return cf;
    }

}
