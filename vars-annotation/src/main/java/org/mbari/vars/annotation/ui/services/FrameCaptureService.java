package org.mbari.vars.annotation.ui.services;

import org.mbari.vars.annotation.etc.jdk.awt.Images;
import org.mbari.vars.annotation.services.ImageCaptureService;
import org.mbari.vars.annotation.model.Framegrab;
import org.mbari.vars.annotation.model.ImageData;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;
import org.mbari.vars.annotation.ui.mediaplayers.MediaPlayer;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.VideoState;
import org.mbari.vars.annotation.etc.jdk.Loggers;

import java.io.File;
import java.time.Instant;

public class FrameCaptureService {

    private static final Loggers log = new Loggers(FrameCaptureService.class);

    /**
     * Capture the current frame from the media player.
     *
     * @return The captured image data. Never null.
     * @throws FrameCaptureException if the frame could not be captured. The message describes why.
     */
    public static ImageData capture(File imageFile,
                                    Media media,
                                    MediaPlayer<? extends VideoState, ? extends VideoError> mediaPlayer) {
        ImageCaptureService service = mediaPlayer.getImageCaptureService();
        if (service == null) {
            throw new FrameCaptureException("The current media player does not support frame capture");
        }

        Framegrab framegrab;
        try {
            framegrab = service.capture(imageFile);
        }
        catch (Exception e) {
            log.atWarn().withCause(e).log("Failed to capture image from " + media.getUri());
            throw new FrameCaptureException(describe(e), e);
        }

        if (framegrab == null || framegrab.getImage().isEmpty()) {
            throw new FrameCaptureException("The media player did not return an image. Unable to read " +
                    imageFile.getAbsolutePath());
        }
        if (framegrab.getVideoIndex().isEmpty()) {
            throw new FrameCaptureException("The media player did not return the video time of the captured frame");
        }

        // If there's an elapsed time, make sure the recordedTimestamp is
        // set and correct. Not all media have a start timestamp.
        if (media.getStartTimestamp() != null) {
            framegrab.getVideoIndex()
                    .flatMap(VideoIndex::getElapsedTime)
                    .ifPresent(elapsedTime -> {
                        Instant recordedDate = media.getStartTimestamp().plus(elapsedTime);
                        framegrab.setVideoIndex(new VideoIndex(elapsedTime, recordedDate));
                    });
        }

        try {
            var bufferedImage = Images.toBufferedImage(framegrab.getImage().get());
            return new ImageData(media.getVideoReferenceUuid(),
                    framegrab.getVideoIndex().get(),
                    bufferedImage);
        }
        catch (Exception e) {
            throw new FrameCaptureException("The captured image could not be processed: " + describe(e), e);
        }
    }

    /**
     * Builds a user-readable message from the exception chain. Wrapper exceptions (e.g.
     * ExecutionException) often have unhelpful messages, so the chain is walked to include
     * the root cause.
     */
    public static String describe(Throwable e) {
        Throwable root = e;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        if (root != e) {
            String rootMsg = root.getMessage() == null ? root.getClass().getSimpleName() : root.getMessage();
            if (!msg.contains(rootMsg)) {
                msg = msg + " (" + rootMsg + ")";
            }
        }
        return msg;
    }

}
