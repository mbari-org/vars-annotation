package org.mbari.vars.ui.services;

import org.mbari.vars.services.ImageCaptureService;
import org.mbari.vars.services.model.Framegrab;
import org.mbari.vars.services.model.Media;
import org.mbari.vars.ui.mediaplayers.MediaPlayer;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.VideoState;

import java.io.File;
import java.time.Instant;
import java.util.Optional;

public class FrameCaptureService {

    public static Optional<Framegrab> capture(File imageFile,
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
            return Optional.of(framegrab);
        } catch (Exception e) {
            // TODO show error
            return Optional.empty();
        }
    }
}
