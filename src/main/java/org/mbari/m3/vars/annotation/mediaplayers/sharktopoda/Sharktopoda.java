package org.mbari.m3.vars.annotation.mediaplayers.sharktopoda;

import org.mbari.m3.vars.annotation.mediaplayers.MediaPlayer;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.vcr4j.VideoIO;
import org.mbari.vcr4j.decorators.SchedulerVideoIO;
import org.mbari.vcr4j.decorators.StatusDecorator;
import org.mbari.vcr4j.decorators.VCRSyncDecorator;
import org.mbari.vcr4j.sharktopoda.SharktopodaError;
import org.mbari.vcr4j.sharktopoda.SharktopodaState;
import org.mbari.vcr4j.sharktopoda.SharktopodaVideoIO;
import org.mbari.vcr4j.sharktopoda.commands.OpenCmd;
import org.mbari.vcr4j.sharktopoda.commands.SharkCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

/**
 * @author Brian Schlining
 * @since 2017-08-10T08:15:00
 */
public class Sharktopoda {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public boolean canOpen(Media media) {
        return false;
    }

    public CompletableFuture<MediaPlayer<SharktopodaState, SharktopodaError>> open(Media media, int sharktopodaPort, int framecapturePort) {
        CompletableFuture<MediaPlayer<SharktopodaState, SharktopodaError>> cf = new CompletableFuture<>();

        try {
            SharktopodaVideoIO videoIO = new SharktopodaVideoIO(UUID.randomUUID(), "localhost", sharktopodaPort);
            videoIO.send(new OpenCmd(media.getUri().toURL()));
            new StatusDecorator<>(videoIO);
            new VCRSyncDecorator<>(videoIO, 1000, 100, 3000000);
            //new FauxTimecodeDecorator(videoIO); // Convert elapsed-time to timecode
            VideoIO<SharktopodaState, SharktopodaError> io =
                    new SchedulerVideoIO<>(videoIO, Executors.newCachedThreadPool());
            MediaPlayer<SharktopodaState, SharktopodaError> newVc =
                    new MediaPlayer<>(media, new SharktopodaImageCaptureService(videoIO, framecapturePort),
                            io, () -> videoIO.send(SharkCommands.CLOSE));
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
