package org.mbari.vars.services;

import org.mbari.vars.services.impl.annosaurus.v2.AnnoServiceV2;

import javax.inject.Inject;
import org.mbari.vars.services.util.PreferencesFactory;

/**
 * @author Brian Schlining
 * @since 2017-06-13T10:56:00
 */
public class Services {

    private final AnnotationService annotationService;
    private final ConceptService conceptService;
    private final ImageArchiveService imageArchiveService;
    private final MediaService mediaService;
    private final UserService userService;
    private final PreferencesService preferencesService;
    private final PreferencesFactory preferencesFactory;

    @Inject
    public Services(AnnotationService annotationService,
                    ConceptService conceptService,
                    ImageArchiveService imageArchiveService,
                    MediaService mediaService,
                    UserService userService,
                    PreferencesService preferencesService,
                    PreferencesFactory preferencesFactory) {
        this.annotationService = annotationService;
        this.conceptService = conceptService;
        this.imageArchiveService = imageArchiveService;
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

    public ImageArchiveService getImageArchiveService() {
        return imageArchiveService;
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
