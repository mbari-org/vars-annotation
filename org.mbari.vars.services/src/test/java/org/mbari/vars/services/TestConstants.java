package org.mbari.vars.services;

import com.google.gson.Gson;
import com.typesafe.config.ConfigFactory;
import org.mbari.vars.services.impl.annosaurus.v1.AnnoWebServiceFactory;
import org.mbari.vars.services.model.Annotation;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.Scanner;

public class TestConstants {

    private static Services services;
    public static synchronized Services getServices() {
        if (services == null) {
            var config = ConfigFactory.load();
            services = ServicesBuilder.build(config);
        }
        return services;
    }

    public static Annotation newTestAnnotation() {
        AnnoWebServiceFactory factory = new AnnoWebServiceFactory("http://deadend.org", Duration.ofMillis(10));
        Gson gson = factory.getGson();
        URL resource = TestConstants.class.getResource("/json/annotation.json");
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
