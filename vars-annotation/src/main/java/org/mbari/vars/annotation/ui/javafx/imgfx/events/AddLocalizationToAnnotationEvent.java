package org.mbari.vars.annotation.ui.javafx.imgfx.events;

import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annosaurus.sdk.r1.models.Association;

/**
 * Signal to add a new localized association to
 */
public record AddLocalizationToAnnotationEvent(Annotation annotation, Association association)
        implements CreateLocalizationEvent {
}
