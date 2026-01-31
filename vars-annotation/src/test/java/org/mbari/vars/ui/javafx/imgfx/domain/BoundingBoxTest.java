package org.mbari.vars.ui.javafx.imgfx.domain;

import org.junit.Test;
import org.mbari.vars.annotation.ui.javafx.imagestage.Json;

import java.util.UUID;

import static org.junit.Assert.*;

public class BoundingBoxTest {

    @Test
    public void testJsonRoundTrip() {
        var json = "{\"x\": 797, \"y\": 914, \"width\": 164, \"height\": 162, \"observer\": \"joost\", \"strength\": \"Padawan\", \"generator\": \"vars-localize\", \"project\": \"FathomNet\", \"image_reference_uuid\": \"b1e8b3cc-9a72-4904-3860-b517e7fca51e\"}";
        var boundingBox = Json.GSON.fromJson(json, BoundingBox.class);
        assertEquals(797L, boundingBox.getX().longValue());
        assertEquals(914L, boundingBox.getY().longValue());
        assertEquals(164L, boundingBox.getWidth().longValue());
        assertEquals(162L, boundingBox.getHeight().longValue());
        assertEquals(UUID.fromString("b1e8b3cc-9a72-4904-3860-b517e7fca51e"), boundingBox.getImageReferenceUuid());
    }
}
