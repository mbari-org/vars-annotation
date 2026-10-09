package org.mbari.vars.annotation.test.ui.javafx.buttons;

import org.junit.jupiter.api.Test;
import org.mbari.vars.annosaurus.sdk.r1.models.Association;
import org.mbari.vars.annosaurus.sdk.r1.models.BoundingBox;
import org.mbari.vars.annotation.services.annosaurus.BoundingBoxes;
import org.mbari.vars.annotation.ui.javafx.buttons.AddLocalizationBC;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AddLocalizationBCTest {

    private static BoundingBox box(Association a) {
        return BoundingBoxes.fromAssociation(a).orElseThrow();
    }

    @Test
    public void createsBoundingBoxAssociation() {
        var a = AddLocalizationBC.centeredBoundingBox(1920, 1080);
        assertEquals(BoundingBox.LINK_NAME, a.getLinkName());
        assertEquals(Association.VALUE_SELF, a.getToConcept());
        assertEquals("application/json", a.getMimeType());
        assertEquals(BoundingBoxes.GENERATOR, box(a).getGenerator());
    }

    @Test
    public void boxIsTenPercentAndCentered() {
        var b = box(AddLocalizationBC.centeredBoundingBox(1920, 1080));
        assertEquals(192, b.getWidth());
        assertEquals(108, b.getHeight());
        assertEquals(864, b.getX()); // (1920 - 192) / 2
        assertEquals(486, b.getY()); // (1080 - 108) / 2
    }

    @Test
    public void tinyVideoStillGetsAVisibleBox() {
        var b = box(AddLocalizationBC.centeredBoundingBox(4, 4));
        assertEquals(1, b.getWidth());
        assertEquals(1, b.getHeight());
    }
}
