package org.mbari.vars.annotation.ui.services;

import org.mbari.vars.annotation.model.ImageData;
import org.mbari.vars.annotation.model.MachineLearningLocalization;

import java.util.Collections;
import java.util.List;

public record MLImageInference(ImageData imageData, List<MachineLearningLocalization> localizations) {


    public MLImageInference(ImageData imageData) {
        this(imageData, Collections.emptyList());
    }

    public MLImageInference copy(List<MachineLearningLocalization> localizations) {
        return new MLImageInference(imageData, localizations);
    }

}


