package org.mbari.vars.ui.javafx.imgfx.events;

import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;

/**
 * Signal to add a new localized association to
 */
public record AddLocalizationToAnnotationEvent(Annotation annotation, Association association)
        implements CreateLocalizationEvent {
}
