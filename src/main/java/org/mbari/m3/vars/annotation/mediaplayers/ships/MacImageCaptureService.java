package org.mbari.m3.vars.annotation.mediaplayers.ships;

import org.mbari.m3.vars.annotation.mediaplayers.macos.AVFImageCaptureService;
import org.mbari.m3.vars.annotation.services.ImageCaptureService;

import java.awt.*;
import java.io.File;
import java.util.Optional;

/**
 * @author Brian Schlining
 * @since 2017-08-11T09:14:00
 */
public class MacImageCaptureService implements ImageCaptureService {

    private final ImageCaptureService proxied;

    public MacImageCaptureService() {
        try {
            proxied = new AVFImageCaptureService()
        }
    }

    @Override
    public Optional<Image> capture(File file) {
        return null;
    }

    @Override
    public void dispose() {

    }

    @Override
    public void showSettingsDialog() {

    }
}
