package org.mbari.m3.vars.annotation.mediaplayers.macos;

import org.mbari.m3.vars.annotation.services.ImageCaptureService;

import java.util.Collection;

/**
 * @author Brian Schlining
 * @since 2018-04-25T11:00:00
 */
public interface SelectableImageCaptureService extends ImageCaptureService {

    Collection<String> listDevices();
    void setDevice(String device);
}
