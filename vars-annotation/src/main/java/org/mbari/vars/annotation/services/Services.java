package org.mbari.vars.annotation.services;

import org.mbari.vars.annosaurus.sdk.r1.AnnotationService;
import org.mbari.vars.annotation.services.oni.PreferencesFactory;
import org.mbari.vars.oni.sdk.r1.ConceptService;
import org.mbari.vars.oni.sdk.r1.PreferencesService;
import org.mbari.vars.oni.sdk.r1.UserService;
import org.mbari.vars.vampiresquid.sdk.r1.MediaService;

public record Services(AnnotationService annotationService,
                       ConceptService conceptService,
                       ImageArchiveService imageArchiveService,
                       MediaService mediaService,
                       UserService userService,
                       PreferencesService preferencesService,
                       PreferencesFactory preferencesFactory) {
}
