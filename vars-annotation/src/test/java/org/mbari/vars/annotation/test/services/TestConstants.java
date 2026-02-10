package org.mbari.vars.annotation.test.services;

import com.google.gson.Gson;
import com.typesafe.config.ConfigFactory;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annotation.etc.gson.Gsons;
import org.mbari.vars.annotation.services.Services;
import org.mbari.vars.annotation.services.VarsServiceFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

public class TestConstants {

    private static Services services;
    public static synchronized Services getServices() {
        if (services == null) {
            var config = ConfigFactory.load();
            var serviceFactory = new VarsServiceFactory(config);
            services = serviceFactory.newServices();
        }
        return services;
    }

    public static Annotation newTestAnnotation() {
        Gson gson = Gsons.SNAKE_CASE_GSON;
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
