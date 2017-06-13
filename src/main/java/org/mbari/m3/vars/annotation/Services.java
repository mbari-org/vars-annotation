package org.mbari.m3.vars.annotation;

import org.mbari.m3.vars.annotation.services.*;
import org.mbari.m3.vars.annotation.util.PreferencesFactory;

import javax.inject.Inject;

/**
 * @author Brian Schlining
 * @since 2017-06-13T10:56:00
 */
public class Services {

    private final AnnotationService annotationService;
    private final ConceptService conceptService;
    private final MediaService mediaService;
    private final UserService userService;
    private final PreferencesService preferencesService;
    private final PreferencesFactory preferencesFactory;

    @Inject
    public Services(AnnotationService annotationService, ConceptService conceptService,
                    MediaService mediaService, UserService userService,
                    PreferencesService preferencesService, PreferencesFactory preferencesFactory) {
        this.annotationService = annotationService;
        this.conceptService = conceptService;
        this.mediaService = mediaService;
        this.userService = userService;
        this.preferencesService = preferencesService;
        this.preferencesFactory = preferencesFactory;
    }

    public AnnotationService getAnnotationService() {
        return annotationService;
    }

    public ConceptService getConceptService() {
        return conceptService;
    }

    public MediaService getMediaService() {
        return mediaService;
    }

    public UserService getUserService() {
        return userService;
    }

    public PreferencesService getPreferencesService() {
        return preferencesService;
    }

    public PreferencesFactory getPreferencesFactory() {
        return preferencesFactory;
    }
}
