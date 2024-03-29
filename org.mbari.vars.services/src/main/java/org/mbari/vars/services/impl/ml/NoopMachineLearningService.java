package org.mbari.vars.services.impl.ml;

import org.mbari.vars.services.MachineLearningService;
import org.mbari.vars.services.model.MachineLearningLocalization;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class NoopMachineLearningService implements MachineLearningService {

    @Override
    public List<MachineLearningLocalization> predict(Path image) {
        return Collections.emptyList();
    }

    @Override
    public List<MachineLearningLocalization> predict(byte[] image) {
        return Collections.emptyList();
    }
}
