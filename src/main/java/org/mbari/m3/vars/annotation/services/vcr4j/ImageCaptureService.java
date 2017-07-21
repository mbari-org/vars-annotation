package org.mbari.m3.vars.annotation.services.vcr4j;

import java.awt.Image;
import java.io.File;
import java.util.Optional;

/**
 * Base interface for services that capture images.
 * @author brian
 */
public interface ImageCaptureService {


    /**
     * The imagecapture service should:
     * 1. Alwasy write to the file, unless it's null. Then write to a temporary file and delete it.
     * 2. Alwasy return the image data
     * @param file
     * @return
     */
    Optional<Image> capture(File file);

    /**
     * Cleanup resources
     */
    void dispose();

    void showSettingsDialog();
}