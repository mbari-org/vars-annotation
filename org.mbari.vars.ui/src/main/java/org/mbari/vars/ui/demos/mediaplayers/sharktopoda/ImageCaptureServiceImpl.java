package org.mbari.vars.ui.demos.mediaplayers.sharktopoda;

import org.mbari.vars.services.model.Framegrab;
import org.mbari.vars.services.ImageCaptureService;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.sharktopoda.SharktopodaVideoIO;
import org.mbari.vcr4j.sharktopoda.commands.FramecaptureCmd;
import org.mbari.vcr4j.sharktopoda.decorators.FramecaptureDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Brian Schlining
 * @since 2017-08-10T08:09:00
 */
public class ImageCaptureServiceImpl implements ImageCaptureService {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final SharktopodaVideoIO io;
    private final FramecaptureDecorator decorator;

    public ImageCaptureServiceImpl(SharktopodaVideoIO io, int port) {
        this.io = io;
        decorator = new FramecaptureDecorator(io, port);
    }

    @Override
    public Framegrab capture(File file) {

        CompletableFuture<Framegrab> future = new CompletableFuture<>();


        // Sharktopoda will send a response once the image is written
        decorator.getFramecaptureObservable()
                .firstElement()
                .subscribe(r -> {

                    Duration elapsedTime = Duration.ofMillis(r.getElapsedTimeMillis());
                    VideoIndex videoIndex = new VideoIndex(elapsedTime);

                    // -- Read file as image
                    BufferedImage image = null;
                    try {
                        image = ImageIO.read(r.getImageLocation());
                    } catch (Exception e) {
                        log.warn("Image capture failed. Unable to read image back off disk", e);
                    }
                    Framegrab framegrab = new Framegrab(image, videoIndex);
                    future.complete(framegrab);
                });

        io.send(new FramecaptureCmd(UUID.randomUUID(), file));

        try {
            return future.get(5, TimeUnit.SECONDS);
        }
        catch (InterruptedException | TimeoutException | ExecutionException e) {
            return new Framegrab();
        }
    }

    @Override
    public void dispose() {
        try {
            decorator.unsubscribe();
        }
        catch (Exception e) {
            // Do nothing.
        }
    }


}
