package org.mbari.vars.annotation.ui.services;

import mbarix4j.awt.image.ImageUtilities;
import org.mbari.vars.annotation.services.ImageCaptureService;
import org.mbari.vars.annotation.model.Framegrab;
import org.mbari.vars.annotation.model.ImageData;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;
import org.mbari.vars.annotation.ui.mediaplayers.MediaPlayer;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.VideoState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Instant;
import java.util.Optional;

public class FrameCaptureService {

    private static final Logger log = LoggerFactory.getLogger(FrameCaptureService.class);

    public static Optional<ImageData> capture(File imageFile,
                                              Media media,
                                              MediaPlayer<? extends VideoState, ? extends VideoError> mediaPlayer) {
        try {
            ImageCaptureService service = mediaPlayer.getImageCaptureService();
            Framegrab framegrab = service.capture(imageFile);

            // If there's an elapsed time, make sure the recordedTimestamp is
            // set and correct
            framegrab.getVideoIndex()
                    .flatMap(VideoIndex::getElapsedTime)
                    .ifPresent(elapsedTime -> {
                        Instant recordedDate = media.getStartTimestamp().plus(elapsedTime);
                        framegrab.setVideoIndex(new VideoIndex(elapsedTime, recordedDate));
                    });
            if (framegrab.isComplete()) {
                var bufferedImage = ImageUtilities.toBufferedImage(framegrab.getImage().get());
                var imageData = new ImageData(media.getVideoReferenceUuid(),
                        framegrab.getVideoIndex().get(),
                        bufferedImage);
                return Optional.of(imageData);
            }
            return Optional.empty();

        } catch (Exception e) {
            log.atWarn().setCause(e).log("Failed capture image from " + media.getUri());
            return Optional.empty();
        }
    }

}
