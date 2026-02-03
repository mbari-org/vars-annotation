package org.mbari.vars.annotation.it.services;

import org.mbari.vars.annotation.services.ServiceBuilder;
import org.mbari.vars.annotation.services.ImageArchiveService;
import org.mbari.vars.annosaurus.sdk.r1.AnnotationService;
import org.mbari.vars.oni.sdk.r1.ConceptService;
import org.mbari.vars.oni.sdk.r1.PreferencesService;
import org.mbari.vars.vampiresquid.sdk.r1.MediaService;

/**
 * Test utility class that provides access to services for integration tests.
 * Uses ServiceBuilder which loads configuration from Raziel.
 *
 * @author Brian Schlining
 * @since 2019-08-28T16:44:00
 */
public class TestToolbox {

    private static final ServiceBuilder serviceBuilder = new ServiceBuilder(false);

    public static AnnotationService getAnnotationService() {
        return serviceBuilder.getAnnotationService();
    }

    public static MediaService getMediaService() {
        return serviceBuilder.getMediaService();
    }

    public static ConceptService getConceptService() {
        return serviceBuilder.getConceptService();
    }

    public static ImageArchiveService getImageArchiveService() {
        return serviceBuilder.getImageArchiveService();
    }

    public static PreferencesService getPreferencesService() {
        return serviceBuilder.getPreferencesService();
    }
}
