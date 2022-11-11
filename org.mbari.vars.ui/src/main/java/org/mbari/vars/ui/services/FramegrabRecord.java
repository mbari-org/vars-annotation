package org.mbari.vars.ui.services;

import org.mbari.vars.services.model.Framegrab;
import org.mbari.vars.services.model.ImageData;
import org.mbari.vars.services.model.MachineLearningLocalization;
import org.mbari.vars.services.util.ImageUtils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public record FramegrabRecord(Path path, ImageData imageData, List<MachineLearningLocalization> localizations) {


    public FramegrabRecord(Path path, ImageData imageData) {
        this(path, imageData, Collections.emptyList());
    }

    public FramegrabRecord copy(List<MachineLearningLocalization> localizations) {
        return new FramegrabRecord(path, imageData, localizations);
    }

}


