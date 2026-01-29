package org.mbari.vars.annotation.ui.javafx.imgfx.events;

import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;

public record DeleteLocalizedAnnotationEvent(Annotation annotation) implements DeleteLocalizationEvent {
}
