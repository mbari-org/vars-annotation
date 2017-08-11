package org.mbari.m3.vars.annotation.mediaplayers.macos;

import org.mbari.m3.vars.annotation.mediaplayers.NoopImageCaptureService;
import org.mbari.m3.vars.annotation.services.ImageCaptureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * We need a singleton to
 * @author Brian Schlining
 * @since 2017-08-11T09:32:00
 */
public class MacImageCaptureServiceRef {

    private static ImageCaptureService imageCaptureService;
    private static final Logger log = LoggerFactory.getLogger(MacImageCaptureServiceRef.class);


    public static ImageCaptureService getImageCaptureService() {
        if (imageCaptureService == null) {
            try {
                imageCaptureService = new AVFImageCaptureService();
            }
            catch (UnsatisfiedLinkError e) {
                log.warn("Failed to instantiate AVFoundation Image Capture.");
                imageCaptureService = new NoopImageCaptureService();
            }
        }
        return imageCaptureService;
    }
}
