package org.mbari.vars.annotation.ui.mediaplayers.sharktopoda2;

import org.mbari.vars.annotation.etc.rxjava.EventBus;
import org.mbari.vars.annotation.services.ImageCaptureService;
import org.mbari.vars.services.model.Framegrab;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.remote.control.commands.FrameCaptureCmd;
import org.mbari.vcr4j.remote.control.commands.FrameCaptureDoneCmd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mbari.vcr4j.remote.control.RVideoIO;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ImageCaptureServiceImpl implements ImageCaptureService {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private RVideoIO io;

    private final EventBus eventBus = new EventBus();

    public ImageCaptureServiceImpl() {
        eventBus.toObserverable()
                .subscribe(obj -> log.debug("Received " + obj.toString()),
                        ex -> log.atWarn().setCause(ex).log("An exception was thrown"),
                        () -> log.info("Closed event bus"));
    }

    public void setIo(RVideoIO io) {
        this.io = io;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    /**
     * The framecapture flow is:
     * 1. method call: capture(file)
     * 2. send framecapture cmd to Sharktopoda
     * 3. listen for framecapturedone cmd from sharkdopoda
     * 4. Push done command to the eventbus in this service
     * 5. Observer for done command, then read data from disk
     * @param file
     * @return
     */
    @Override
    public Framegrab capture(File file) {
        if (io != null) {

            var observable = eventBus.toObserverable()
                    .ofType(FrameCaptureDoneCmd.class)
//                    .observeOn(Schedulers.io())
                    .map(this::captureDone);
            // log.atWarn().log("Sending frame capture command to Sharktopoda. Saving image to {}", file.getAbsolutePath());
            io.send(new FrameCaptureCmd(io.getUuid(), UUID.randomUUID(), file.getAbsolutePath()));
            // log.atWarn().log("Waiting for frame capture done command from Sharktopoda");
            return observable.timeout(10, TimeUnit.SECONDS).blockingFirst();
//            return observable.blockingFirst();
        }
        else {
            throw new IllegalStateException("The video io object is null. Unable to send a command to Sharktopoda");
        }
    }

    public final Framegrab captureDone(FrameCaptureDoneCmd cmd) {
        var request = cmd.getValue();
        var elapsedTime = Duration.ofMillis(request.getElapsedTimeMillis());
        var videoIndex = new VideoIndex(elapsedTime);
        BufferedImage image = null;
        var imageLocation = new File(request.getImageLocation());
        try {
            image = ImageIO.read(imageLocation);
        } catch (Exception e) {
            log.warn("Image capture failed. Unable to read image back off disk", e);
        }
        log.atDebug().log("Image capture complete. Read " + imageLocation.length() + " bytes from " + imageLocation.getAbsolutePath());
        return new Framegrab(image, videoIndex);
    }

    @Override
    public void dispose() {
    }
}
