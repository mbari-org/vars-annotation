package org.mbari.vars.annotation.ui.mediaplayers;

import org.mbari.vars.annotation.model.Framegrab;
import org.mbari.vars.annotation.services.ImageCaptureService;

import java.io.File;

/**
 * @author Brian Schlining
 * @since 2017-08-11T09:12:00
 */
public class NoopImageCaptureService implements ImageCaptureService {

    @Override
    public Framegrab capture(File file) {
        return new Framegrab();
    }

    @Override
    public void dispose() {
        // Do nothing
    }


}
