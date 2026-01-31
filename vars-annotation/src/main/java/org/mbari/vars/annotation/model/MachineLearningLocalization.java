package org.mbari.vars.annotation.model;

import org.mbari.vars.annosaurus.sdk.r1.models.BoundingBox;

public record MachineLearningLocalization(String concept, Double confidence, BoundingBox boundingBox) {}
