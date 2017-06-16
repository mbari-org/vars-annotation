package org.mbari.m3.vars.annotation.ui;

import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Authorization;
import org.mbari.m3.vars.annotation.services.*;
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

    private static final UIToolBox toolBox = Initializer.getToolBox();

    private static CachedConceptService conceptService;
    public static EventBus EVENT_BUS = toolBox.getEventBus();

    public static ResourceBundle UI_BUNDLE = toolBox.getI18nBundle();

    public static UIToolBox getToolBox() {
        return toolBox;
    }

    public static final ConceptService newConceptService() {
        return getToolBox().getServices().getConceptService();
    }

    public static final MediaService newMediaService() {
        return getToolBox().getServices().getMediaService();
    }

    public static final AnnotationService newAnnotationService() {
        return getToolBox().getServices().getAnnotationService();
    }


}
