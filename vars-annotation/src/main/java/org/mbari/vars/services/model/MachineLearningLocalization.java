package org.mbari.vars.services.model;

public record MachineLearningLocalization(String concept, Double confidence, BoundingBox boundingBox) {}
