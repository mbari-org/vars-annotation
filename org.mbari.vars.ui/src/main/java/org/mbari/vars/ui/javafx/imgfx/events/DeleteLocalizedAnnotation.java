package org.mbari.vars.ui.javafx.imgfx.events;

import org.mbari.vars.services.model.Annotation;

public record DeleteLocalizedAnnotation(Annotation annotation) implements DeleteLocalizationEvent {
}
