package org.mbari.vars.ui.javafx;

import org.mbari.vars.services.model.BoundingBox;
import org.mbari.vars.services.model.Image;
import org.mbari.vars.services.model.Media;
import org.mbari.vars.ui.UIToolBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BoundingBoxDecorator {
    private final UIToolBox toolBox;
    private final Duration timeout;
    private static final Logger log = LoggerFactory.getLogger(BoundingBoxDecorator.class);
    private static final double EPSILON = 0.01;

    public BoundingBoxDecorator(UIToolBox toolBox, Duration timeout) {
        this.toolBox = toolBox;
        this.timeout = timeout;
    }

    public double estimateScale(UUID videoReferenceUuid, UUID imageReferenceUuid, int targetWidth, int targetHeight) {
        double xScale = 1;
        double yScale = 1;

        boolean keepGoing = true;

        // If there's an imageReferenceUuid weill use that as the source dimensions for the image that
        // the bounding box belongs to.
        if (imageReferenceUuid != null) {
            final Image image;
            try {
                image = toolBox.getServices()
                        .getAnnotationService()
                        .findImageByUuid(imageReferenceUuid)
                        .get(timeout.toMillis(), TimeUnit.MILLISECONDS);
                if (image != null) {
                    xScale = targetWidth / (double) image.getWidth();
                    yScale = targetHeight / (double) image.getHeight();
                    keepGoing = false;
                }
                else {
                    log.atWarn().log(() -> "Unable to find the bounding box image. Falling back to using it's video size");
                }
            } catch (Exception e) {
                log.atWarn().setCause(e).log("Failed to look up image with UUID = " + imageReferenceUuid
                    + ". Falling back to using it's video size");
            }
        }

        // If we made it into this block, we're using the video size that the bounding box belongs to.
        if (keepGoing) {
            final Media media;
            try {
                media = toolBox.getServices()
                        .getMediaService()
                        .findByUuid(videoReferenceUuid)
                        .get(timeout.toMillis(), TimeUnit.MILLISECONDS);
                if (media != null) {
                    xScale = targetWidth / media.getWidth().doubleValue();
                    yScale = targetHeight / media.getHeight().doubleValue();
                }
            } catch (Exception e) {
                log.atWarn().log(() -> "Unable to find the bounding box image. We're punting and using a scale of 1");
            }
        }

        if (Math.abs(xScale - yScale) > EPSILON) {
            log.atWarn().log(() -> "The image aspect ratio is not 1. This is a problem!");
        }
        return xScale;

    }
}
