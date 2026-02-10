package org.mbari.vars.annotation.services;

import org.mbari.vars.annotation.model.MachineLearningLocalization;

import java.nio.file.Path;
import java.util.List;

public interface MachineLearningService {

    List<MachineLearningLocalization> predict(Path image);

    List<MachineLearningLocalization> predict(byte[] jpegBytes);

}
