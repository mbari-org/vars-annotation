package org.mbari.m3.vars.annotation.services.annosaurus.v1;

import com.google.gson.Gson;
import static org.junit.Assert.*;
import org.junit.Test;
import org.mbari.m3.vars.annotation.model.Annotation;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.Scanner;

/**
 * @author Brian Schlining
 * @since 2017-08-07T14:57:00
 */
public class GSONTest {

    @Test
    public void fromJson() {
        AnnoWebServiceFactory factory = new AnnoWebServiceFactory("http://deadend.org", Duration.ofMillis(10));
        Gson gson = factory.getGson();
        URL resource = getClass().getResource("/json/annotation.json");
        try {
            InputStream stream = resource.openStream();
            Scanner scanner = new Scanner(stream, "UTF-8");
            String s = scanner.useDelimiter("\\A").next();
            scanner.close();
            stream.close();
            Annotation annotation = gson.fromJson(s, Annotation.class);
            System.out.println(annotation);
            assertTrue(annotation.getImages() != null);
            assertTrue(annotation.getImages().size() == 2);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
