package org.mbari.vars.services;

import org.mbari.vars.services.model.MachineLearningLocalization;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.List;

public interface MachineLearningService {

    List<MachineLearningLocalization> predict(Path image);

    List<MachineLearningLocalization> predict(BufferedImage image);

}
