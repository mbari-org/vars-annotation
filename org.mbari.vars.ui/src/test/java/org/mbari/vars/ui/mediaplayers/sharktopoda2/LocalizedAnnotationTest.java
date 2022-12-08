package org.mbari.vars.ui.mediaplayers.sharktopoda2;

import org.junit.Test;
import static org.junit.Assert.*;

import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;
import org.mbari.vars.services.model.BoundingBox;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.remote.control.commands.localization.Localization;
import org.mbari.vcr4j.sharktopoda.client.model.Video;

import java.time.Duration;
import java.util.UUID;

public class LocalizedAnnotationTest {

    private Annotation annotation = new Annotation("Nanomia bijuga", "brian",
            new VideoIndex(Duration.ofMillis(12345L)),
            UUID.randomUUID());
    private Association association = new Association(BoundingBox.LINK_NAME,
            Association.VALUE_SELF,
            """{"x": 10, "y":20, "width": 30, "height"
                    """)
    private Localization localization = new Localization(UUID.randomUUID(),
            annotation.getConcept(),
            12345L, 987L, 10, 20, 30, 40, "#123456");

    @Test
    public void toLocalizationTest() {

    }
}
