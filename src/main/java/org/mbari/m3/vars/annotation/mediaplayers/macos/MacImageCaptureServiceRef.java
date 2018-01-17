package org.mbari.m3.vars.annotation.mediaplayers.macos;

import org.mbari.m3.vars.annotation.mediaplayers.NoopImageCaptureService;
import org.mbari.m3.vars.annotation.model.Framegrab;
import org.mbari.m3.vars.annotation.services.ImageCaptureService;
import org.mbari.vcr4j.VideoIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * We need a singleton to
 * @author Brian Schlining
 * @since 2017-08-11T09:32:00
 */
public class MacImageCaptureServiceRef implements ImageCaptureService {

    private static ImageCaptureService imageCaptureService;
    private static final Logger log = LoggerFactory.getLogger(MacImageCaptureServiceRef.class);

    private static ImageCaptureService getImageCaptureService() {
        if (imageCaptureService == null) {
            try {
                imageCaptureService = AVFImageCaptureService.getInstance();
            }
            catch (UnsatisfiedLinkError e) {
                log.warn("Failed to instantiate AVFoundation Image Capture.");
                imageCaptureService = new NoopImageCaptureService();
            }
        }
        return imageCaptureService;
    }

    @Override
    public Framegrab capture(File file) {
        ImageCaptureService ics = getImageCaptureService();
        if (ics instanceof AVFImageCaptureService) {
            // Lookup selected device from settings everytime we run
            // TODO avf currently uses local computer time for video-index.
            // That only works for real-time annotations.
            AVFImageCaptureService avf = (AVFImageCaptureService) imageCaptureService;
            String selectedDevice = SettingsPaneImpl.getSelectedDevice();
            if (selectedDevice == null || selectedDevice.isEmpty()) {
                log.warn("No image capture device has been selected in settings");
            }
            else {
                avf.setDevice(selectedDevice);
            }

        }
        return ics.capture(file);
    }

    @Override
    public void dispose() {
        getImageCaptureService().dispose();
    }
}
