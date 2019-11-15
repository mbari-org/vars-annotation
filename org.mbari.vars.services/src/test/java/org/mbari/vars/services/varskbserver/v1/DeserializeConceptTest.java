package org.mbari.vars.services.varskbserver.v1;

import org.junit.Assert;
import org.junit.Test;
import org.mbari.vars.services.impl.varskbserver.v1.KBWebServiceFactory;
import org.mbari.vars.services.model.Concept;

import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.Executors;

/**
 * @author Brian Schlining
 * @since 2019-11-14T15:20:00
 */
public class DeserializeConceptTest {

    private KBWebServiceFactory factory = new KBWebServiceFactory("http://www.foo.org",
            Duration.ofSeconds(10),
            Executors.newSingleThreadExecutor());

    @Test
    public void deserializeTreeTest() {
        try {
            URI resource = getClass().getResource("/json/nanomia.json").toURI();
            var json = Files.readString(Paths.get(resource), StandardCharsets.UTF_8);
            var root = factory.getGson().fromJson(json, Concept.class);
            Assert.assertNotNull(root);
            Assert.assertEquals("object", root.getName());
            Assert.assertNotNull(root.getAlternativeNames());
            Assert.assertEquals(1, root.getAlternativeNames().size());
            Assert.assertNotNull(root.getChildren());
            Assert.assertEquals(1, root.getChildren().size());

        }
        catch (Exception e) {
            Assert.fail("An exception occurred");
        }
    }
}
