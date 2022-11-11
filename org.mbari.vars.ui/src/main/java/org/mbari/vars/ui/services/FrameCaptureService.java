package org.mbari.vars.ui.services;

import org.mbari.vars.services.ImageCaptureService;
import org.mbari.vars.services.model.Framegrab;
import org.mbari.vars.services.model.ImageData;
import org.mbari.vars.services.model.Media;
import org.mbari.vars.ui.mediaplayers.MediaPlayer;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.VideoState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
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
                var imageData = new ImageData(media.getVideoReferenceUuid(),
                        framegrab.getVideoIndex().get(),
                        (BufferedImage) framegrab.getImage().get());
                return Optional.of(imageData);
            }
            return Optional.empty();

        } catch (Exception e) {
            log.atWarn().setCause(e).log("Failed capture image from " + media.getUri());
            return Optional.empty();
        }
    }

}
