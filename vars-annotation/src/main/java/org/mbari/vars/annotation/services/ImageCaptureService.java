package org.mbari.vars.annotation.services;

import org.mbari.vars.services.model.Framegrab;

import java.io.File;

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
    Framegrab capture(File file);

    /**
     * Cleanup resources
     */
    void dispose();

}