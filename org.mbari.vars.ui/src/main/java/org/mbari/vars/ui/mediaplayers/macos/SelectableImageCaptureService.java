package org.mbari.vars.ui.mediaplayers.macos;

import org.mbari.vars.services.ImageCaptureService;

import java.util.Collection;

/**
 * @author Brian Schlining
 * @since 2018-04-25T11:00:00
 */
public interface SelectableImageCaptureService extends ImageCaptureService {

    Collection<String> listDevices();
    void setDevice(String device);
}
