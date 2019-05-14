package org.mbari.m3.vars.annotation;

import org.mbari.m3.vars.annotation.services.*;
import org.mbari.m3.vars.annotation.services.annosaurus.v2.AnnoServiceV2;
import org.mbari.m3.vars.annotation.util.PreferencesFactory;

import javax.inject.Inject;

/**
 * @author Brian Schlining
 * @since 2017-06-13T10:56:00
 */
public class Services {

    private final AnnotationService annotationService;
    private final AnnoServiceV2 annoServiceV2;
    private final ConceptService conceptService;
    private final ImageArchiveService imageArchiveService;
    private final MediaService mediaService;
    private final UserService userService;
    private final PreferencesService preferencesService;
    private final PreferencesFactory preferencesFactory;

    @Inject
    public Services(AnnotationService annotationService,
                    AnnoServiceV2 annoServiceV2,
                    ConceptService conceptService,
                    ImageArchiveService imageArchiveService,
                    MediaService mediaService,
                    UserService userService,
                    PreferencesService preferencesService,
                    PreferencesFactory preferencesFactory) {
        this.annotationService = annotationService;
        this.annoServiceV2 = annoServiceV2;
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

    public AnnoServiceV2 getAnnoServiceV2() {
        return annoServiceV2;
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
