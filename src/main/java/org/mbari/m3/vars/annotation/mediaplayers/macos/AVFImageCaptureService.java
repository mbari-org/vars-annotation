package org.mbari.m3.vars.annotation.mediaplayers.macos;

import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.mediaplayers.NoopImageCaptureService;
import org.mbari.m3.vars.annotation.model.Framegrab;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.services.ImageCaptureService;
import org.mbari.vars.avfoundation.AVFImageCapture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

/**
 * This class prvides and ImageCaptureService facade to the AVFoundation code from the
 * vars-avfoundation library.
 * @author Brian Schlining
 * @since 2017-08-11T09:16:00
 */
public class AVFImageCaptureService implements ImageCaptureService {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private AVFImageCapture imageCapture;

    public AVFImageCaptureService() {
        imageCapture = new AVFImageCapture();
    }

    public Collection<String> listDevices() {
        return Arrays.asList(imageCapture.videoDevicesAsStrings());
    }

    public void setDevice(String device) {
        dispose();
        imageCapture.startSession(device);
    }

    @Override
    public Framegrab capture(File file) {
        Framegrab framegrab = new Framegrab();
        imageCapture.capture(file)
                .ifPresent(framegrab::setImage);
        return framegrab;
    }

    @Override
    public void dispose() {
        try {
            imageCapture.stopSession();
        }
        catch (Exception e) {
            log.error("An error occurred while stopping the AVFoundation image capture", e);
        }
    }

    @Override
    public void showSettingsDialog() {
                // TODO
    }

}
