package org.mbari.m3.vars.annotation.mediaplayers.macos;

import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.mediaplayers.MediaPlayer;
import org.mbari.m3.vars.annotation.mediaplayers.NoopImageCaptureService;
import org.mbari.m3.vars.annotation.model.Framegrab;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.services.ImageCaptureService;
import org.mbari.vars.avfoundation.AVFImageCapture;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.VideoState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * This class prvides and ImageCaptureService facade to the AVFoundation code from the
 * vars-avfoundation library.
 * @author Brian Schlining
 * @since 2017-08-11T09:16:00
 */
public class AVFImageCaptureService implements SelectableImageCaptureService {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private AVFImageCapture imageCapture;
    public static AVFImageCaptureService imageCaptureService;
    private String currentDevice = "";

    protected AVFImageCaptureService() {
        imageCapture = new AVFImageCapture();
    }

    public Collection<String> listDevices() {
        return Arrays.asList(imageCapture.videoDevicesAsStrings());
    }

    public void setDevice(String device) {
        currentDevice = device;
    }

    @Override
    public Framegrab capture(File file) {
        Framegrab framegrab = new Framegrab();
        imageCapture.startSessionWithNamedDevice(currentDevice);
        Optional<Image> imageOpt = imageCapture.capture(file, Duration.ofSeconds(10));
        if (imageOpt.isPresent()) {
            framegrab.setImage(imageOpt.get());

            MediaPlayer<? extends VideoState, ? extends VideoError> mediaPlayer = Initializer.getToolBox().getMediaPlayer();
            if (mediaPlayer != null) {
                try {

                    // HACK - Use a 3 second timeout
                    mediaPlayer.requestVideoIndex()
                            .thenAccept(framegrab::setVideoIndex)
                            .get(3000, TimeUnit.MILLISECONDS);
                }
                catch (Exception e) {
                    log.warn("Problem with requesting videoIndex while capturing a framegrab", e);
                    framegrab.setVideoIndex(new VideoIndex(Instant.now()));
                }
            }

            // If, for some reason, getting the video index fails. Fall back to a timestamp
            if (!framegrab.getVideoIndex().isPresent()) {
                log.warn("Failed to get video index. Using current timestamp for video index");
                framegrab.setVideoIndex(new VideoIndex(Instant.now()));
            }


        }
        else {
            log.warn("Failed to capture image from device named '" +
                    currentDevice + "'");
        }
        imageCapture.stopSession();
        return framegrab;
    }

    @Override
    public void dispose() {
//        try {
//            imageCapture.stopSession();
//        }
//        catch (UnsatisfiedLinkError | Exception e) {
//            log.error("An error occurred while stopping the AVFoundation image capture", e);
//        }
    }


    /**
     * We only want one isntance on any machine as the native libraries will
     * interfere if more than one instance is active.
     * @return
     */
    public static synchronized AVFImageCaptureService getInstance() {
        if (imageCaptureService == null) {
            imageCaptureService = new AVFImageCaptureService();
        }
        return imageCaptureService;
    }

    public AVFImageCapture getImageCapture() {
        return imageCapture;
    }
}
