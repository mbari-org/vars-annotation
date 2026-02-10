package org.mbari.vars.annotation.ui.javafx.imgfx.events;

import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annosaurus.sdk.r1.models.Association;

public record DeleteLocalizedAssociationEvent(Annotation annotation, Association association)
    implements DeleteLocalizationEvent {
}
