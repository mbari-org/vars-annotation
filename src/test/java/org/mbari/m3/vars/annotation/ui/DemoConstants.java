package org.mbari.m3.vars.annotation.ui;

import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Authorization;
import org.mbari.m3.vars.annotation.services.AnnotationService;
import org.mbari.m3.vars.annotation.services.BasicJWTAuthService;
import org.mbari.m3.vars.annotation.services.CachedConceptService;
import org.mbari.m3.vars.annotation.services.MediaService;
import org.mbari.m3.vars.annotation.services.annosaurus.v1.AnnoService;
import org.mbari.m3.vars.annotation.services.annosaurus.v1.AnnoWebServiceFactory;
import org.mbari.m3.vars.annotation.services.vampiresquid.v1.VamService;
import org.mbari.m3.vars.annotation.services.vampiresquid.v1.VamWebServiceFactory;
import org.mbari.m3.vars.annotation.services.varskbserver.v1.KBConceptService;
import org.mbari.m3.vars.annotation.services.varskbserver.v1.KBWebServiceFactory;

import java.util.ResourceBundle;

/**
 * @author Brian Schlining
 * @since 2017-06-03T18:43:00
 */
public class DemoConstants {

    public static final String CONCEPT_ENDPOINT = "http://m3.shore.mbari.org/kb/v1/";
    public static final String MEDIA_ENDPOINT = "http://m3.shore.mbari.org/vam/v1";
    public static final String ANNOTATION_ENDPOINT = "http://m3.shore.mbari.org/anno/v1";

    private static final UIToolBox toolBox = Initializer.getToolBox();

    private static CachedConceptService conceptService;
    public static EventBus EVENT_BUS = toolBox.getEventBus();

    public static ResourceBundle UI_BUNDLE = toolBox.getI18nBundle();

    public static UIToolBox getToolBox() {
        return toolBox;
    }

    public static final CachedConceptService newConceptService() {
        if (conceptService == null) {
            conceptService = new CachedConceptService(
                    new KBConceptService(new KBWebServiceFactory("http://m3.shore.mbari.org/kb/v1/")));
        }
        return conceptService;
    }

    public static final MediaService newMediaService() {
        Authorization auth = new Authorization("BEARER", "foo");
        VamWebServiceFactory factory = new VamWebServiceFactory("http://m3.shore.mbari.org/vam/v1");
        return new VamService(factory, new BasicJWTAuthService(factory, auth));
    }

    public static final AnnotationService newAnnotationService() {
        Authorization auth = new Authorization("BEARER", "foo");
        final AnnoWebServiceFactory factory = new AnnoWebServiceFactory(ANNOTATION_ENDPOINT);
        return new AnnoService(factory, new BasicJWTAuthService(factory, auth));
    }
}
