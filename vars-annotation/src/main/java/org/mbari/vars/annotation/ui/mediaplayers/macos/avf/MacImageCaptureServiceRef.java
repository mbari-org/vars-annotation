package org.mbari.vars.ui.mediaplayers.macos.avf;

import org.mbari.vars.services.model.Framegrab;
import org.mbari.vars.services.ImageCaptureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * We need a singleton to
 * @author Brian Schlining
 * @since 2017-08-11T09:32:00
 */
public class MacImageCaptureServiceRef implements ImageCaptureService {

    private static final Logger log = LoggerFactory.getLogger(MacImageCaptureServiceRef.class);

    private static ImageCaptureService getImageCaptureService() {

        String captureApi = CaptureApiSettings.getSelectedCaptureApi();
        String deviceName = CaptureApiSettings.getSelectedDevice();
        CaptureApi api = CaptureApi.findByName(captureApi);

        SelectableImageCaptureService imageCaptureService = CaptureApi.NONE.getImageCaptureService();
        try {
            imageCaptureService = api.getImageCaptureService();
        }
        catch (UnsatisfiedLinkError e) {
            log.warn("Failed to instantiate image capture service for " + api.getName());
        }
        try {
            imageCaptureService.setDevice(deviceName);
        }
        catch (Exception e) {
            log.warn("Failed to set device to " + deviceName + " for the " +
                    api.getName() + " API", e);
        }
        return imageCaptureService;
    }

    @Override
    public Framegrab capture(File file) {
        return getImageCaptureService().capture(file);
    }

    @Override
    public void dispose() {
        getImageCaptureService().dispose();
    }
}
