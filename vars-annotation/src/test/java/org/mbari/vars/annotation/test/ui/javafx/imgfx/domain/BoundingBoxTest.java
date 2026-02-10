package org.mbari.vars.annotation.test.ui.javafx.imgfx.domain;



import org.junit.jupiter.api.Test;
import org.mbari.vars.annosaurus.sdk.r1.models.BoundingBox;
import org.mbari.vars.annotation.etc.gson.Gsons;

import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;


public class BoundingBoxTest {

    @Test
    public void testJsonRoundTrip() {
        var json = "{\"x\": 797, \"y\": 914, \"width\": 164, \"height\": 162, \"observer\": \"joost\", \"strength\": \"Padawan\", \"generator\": \"vars-localize\", \"project\": \"FathomNet\", \"image_reference_uuid\": \"b1e8b3cc-9a72-4904-3860-b517e7fca51e\"}";
        var boundingBox = Gsons.SNAKE_CASE_GSON.fromJson(json, BoundingBox.class);
        assertEquals(797, boundingBox.getX());
        assertEquals(914, boundingBox.getY());
        assertEquals(164, boundingBox.getWidth());
        assertEquals(162, boundingBox.getHeight());
        assertEquals(UUID.fromString("b1e8b3cc-9a72-4904-3860-b517e7fca51e"), boundingBox.getImageReferenceUuid());
    }
}
