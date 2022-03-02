package org.mbari.vars.ui.javafx.imgfx.events;

import org.mbari.vars.services.model.Annotation;

public record DeleteLocalizedAnnotationEvent(Annotation annotation) implements DeleteLocalizationEvent {
}
