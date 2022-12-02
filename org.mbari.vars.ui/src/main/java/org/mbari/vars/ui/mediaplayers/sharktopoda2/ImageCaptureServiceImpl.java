package org.mbari.vars.ui.mediaplayers.sharktopoda2;

import io.reactivex.rxjava3.schedulers.Schedulers;
import org.mbari.vars.core.EventBus;
import org.mbari.vars.services.ImageCaptureService;
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

public class ImageCaptureServiceImpl implements ImageCaptureService {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private RVideoIO io;

    private final EventBus eventBus = new EventBus();

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
        var observable = eventBus.toObserverable()
                .ofType(FrameCaptureDoneCmd.class)
                .observeOn(Schedulers.io())
                .map(this::captureDone);

        io.send(new FrameCaptureCmd(io.getUuid(), UUID.randomUUID(), file.getAbsolutePath()));
        return observable.blockingFirst();
    }

    public final Framegrab captureDone(FrameCaptureDoneCmd cmd) {
        var request = cmd.getValue();
        var elapsedTime = Duration.ofMillis(request.getElapsedTimeMillis());
        var videoIndex = new VideoIndex(elapsedTime);
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(request.getImageLocation()));
        } catch (Exception e) {
            log.warn("Image capture failed. Unable to read image back off disk", e);
        }
        return new Framegrab(image, videoIndex);
    }

    @Override
    public void dispose() {
    }
}
