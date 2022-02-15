package org.mbari.vars.ui.javafx.imgfx.events;

import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;

/**
 * Signal to create a new localized annotation
 * @param annotation The new annotation we're creating
 * @param association The association containing the localization info
 */
public record CreateNewLocalizedAnnotationEvent(Annotation annotation, Association association)
    implements CreateLocalizationEvent {
}
