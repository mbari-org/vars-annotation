package org.mbari.vars.annotation.services.ml;

import org.mbari.vars.annosaurus.sdk.r1.models.BoundingBox;
import org.mbari.vars.annotation.model.MachineLearningLocalization;

import java.util.ArrayList;
import java.util.List;

public class MachineLearningResponse1 {

    private Boolean success;
    private List<MachineLearningPrediction1> predictions;

    public MachineLearningResponse1() {
    }

    public MachineLearningResponse1(Boolean success, List<MachineLearningPrediction1> predictions) {
        this.success = success;
        this.predictions = predictions;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public List<MachineLearningPrediction1> getPredictions() {
        return predictions;
    }

    public void setPredictions(List<MachineLearningPrediction1> predictions) {
        this.predictions = new ArrayList<>(predictions);
    }

    public List<MachineLearningLocalization> toMLStandard() {
        return predictions.stream()
                .map(p -> {
                    var box = new BoundingBox(p.getX(), p.getY(), p.getWidth(), p.getHeight());
                    return new MachineLearningLocalization(p.getCategoryId(), p.getScores().getFirst(), box);
                })
                .toList();
    }
}
