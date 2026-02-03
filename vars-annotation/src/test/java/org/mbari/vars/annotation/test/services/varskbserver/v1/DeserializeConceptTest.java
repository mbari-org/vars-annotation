package org.mbari.vars.annotation.test.services.varskbserver.v1;

import org.junit.jupiter.api.Test;
import org.mbari.vars.annotation.etc.gson.Gsons;
import org.mbari.vars.oni.sdk.r1.models.Concept;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
/**
 * @author Brian Schlining
 * @since 2019-11-14T15:20:00
 */
public class DeserializeConceptTest {

    @Test
    public void deserializeTreeTest() {
        try {
            URI resource = getClass().getResource("/json/nanomia.json").toURI();
            var json = Files.readString(Paths.get(resource), StandardCharsets.UTF_8);
            var gson = Gsons.CAMEL_CASE_GSON;
            var root = gson.fromJson(json, Concept.class);
            assertNotNull(root);
            assertEquals("object", root.getName());
            assertNotNull(root.getAlternativeNames());
            assertEquals(1, root.getAlternativeNames().size());
            assertNotNull(root.getChildren());
            assertEquals(1, root.getChildren().size());

        }
        catch (Exception e) {
            fail("An exception occurred");
        }
    }
}
