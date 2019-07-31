package org.mbari.vars.ui.demos.javafx;

import com.google.gson.Gson;
import org.mbari.vars.ui.Initializer;
import org.mbari.vars.ui.EventBus;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.services.*;
import org.mbari.vars.services.impl.annosaurus.v1.AnnoWebServiceFactory;
import org.mbari.vars.services.model.Annotation;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.ResourceBundle;
import java.util.Scanner;


/**
 * @author Brian Schlining
 * @since 2017-06-03T18:43:00
 */
public class DemoConstants {

    private static final UIToolBox toolBox = Initializer.getToolBox();

    public static EventBus EVENT_BUS = toolBox.getEventBus();

    public static ResourceBundle UI_BUNDLE = toolBox.getI18nBundle();

    public static UIToolBox getToolBox() {
        return toolBox;
    }

    public static ConceptService newConceptService() {
        return getToolBox().getServices().getConceptService();
    }

    public static MediaService newMediaService() {
        return getToolBox().getServices().getMediaService();
    }

    public static AnnotationService newAnnotationService() {
        return getToolBox().getServices().getAnnotationService();
    }


    public static Annotation newTestAnnotation() {
        AnnoWebServiceFactory factory = new AnnoWebServiceFactory("http://deadend.org", Duration.ofMillis(10));
        Gson gson = factory.getGson();
        URL resource = DemoConstants.class.getResource("/json/annotation.json");
        try {
            InputStream stream = resource.openStream();
            Scanner scanner = new Scanner(stream, "UTF-8");
            String s = scanner.useDelimiter("\\A").next();
            scanner.close();
            stream.close();
            return gson.fromJson(s, Annotation.class);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
