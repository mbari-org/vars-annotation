package org.mbari.vars.ui.services;

import org.mbari.vars.services.model.ImageData;
import org.mbari.vars.services.model.MachineLearningLocalization;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public record MLImageInference(ImageData imageData, List<MachineLearningLocalization> localizations) {


    public MLImageInference(Path path, ImageData imageData) {
        this(imageData, Collections.emptyList());
    }

    public MLImageInference copy(List<MachineLearningLocalization> localizations) {
        return new MLImageInference(imageData, localizations);
    }

}


