package org.mbari.m3.vars.annotation.mediaplayers;

import org.mbari.m3.vars.annotation.model.Framegrab;
import org.mbari.m3.vars.annotation.services.ImageCaptureService;

import java.awt.*;
import java.io.File;
import java.util.Optional;

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

    @Override
    public void showSettingsDialog() {
        // Do nothing ... may show empty dialog if we decide to implement this
    }
}
