package org.mbari.m3.vars.annotation.mediaplayers.macos;

import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.mediaplayers.NoopImageCaptureService;
import org.mbari.m3.vars.annotation.model.Framegrab;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.services.ImageCaptureService;
import org.mbari.vars.avfoundation.AVFImageCapture;
import org.mbari.vcr4j.VideoIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * This class prvides and ImageCaptureService facade to the AVFoundation code from the
 * vars-avfoundation library.
 * @author Brian Schlining
 * @since 2017-08-11T09:16:00
 */
public class AVFImageCaptureService implements ImageCaptureService {

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
            // HACK - This only works for real-time annotation.
            // FIXME - Change this for tape annotation
            framegrab.setVideoIndex(new VideoIndex(Instant.now()));
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
