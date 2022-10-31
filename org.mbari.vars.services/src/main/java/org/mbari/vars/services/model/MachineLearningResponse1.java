package org.mbari.vars.services.model;

import java.util.ArrayList;
import java.util.List;

public class MachineLearningResponse1 {

    private Boolean success;
    private List<MachineLearningResponse1> predictions;

    public MachineLearningResponse1() {
    }

    public MachineLearningResponse1(Boolean success, List<MachineLearningResponse1> predictions) {
        this.success = success;
        this.predictions = predictions;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public List<MachineLearningResponse1> getPredictions() {
        return predictions;
    }

    public void setPredictions(List<MachineLearningResponse1> predictions) {
        this.predictions = new ArrayList<>(predictions);
    }
}
