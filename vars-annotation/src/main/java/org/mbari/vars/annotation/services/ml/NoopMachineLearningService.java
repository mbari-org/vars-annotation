package org.mbari.vars.annotation.services.ml;

import org.mbari.vars.annotation.services.MachineLearningService;
import org.mbari.vars.annotation.model.MachineLearningLocalization;

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
