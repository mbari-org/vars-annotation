package org.mbari.vars.ui.javafx.imgfx.events;

import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;

public record DeleteLocalizedAssociationEvent(Annotation annotation, Association association)
    implements DeleteLocalizationEvent {
}
