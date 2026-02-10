package org.mbari.vars.annotation.ui.mediaplayers.macos.avf;

import org.mbari.vars.annotation.etc.jdk.Loggers;
import org.mbari.vars.annotation.model.Framegrab;
import org.mbari.vars.annotation.services.ImageCaptureService;

import java.io.File;

/**
 * We need a singleton to
 * @author Brian Schlining
 * @since 2017-08-11T09:32:00
 */
public class MacImageCaptureServiceRef implements ImageCaptureService {

    private static final Loggers log = new Loggers(MacImageCaptureServiceRef.class);

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
            log.atWarn().withCause(e).log("Failed to set device to " + deviceName + " for the " +
                    api.getName() + " API");
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
