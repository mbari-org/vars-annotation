package org.mbari.vars.annotation.test.ui.mediaplayers.sharktopoda2;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annosaurus.sdk.r1.models.Association;
import org.mbari.vars.annosaurus.sdk.r1.models.BoundingBox;
import org.mbari.vars.annotation.ui.mediaplayers.sharktopoda2.LocalizedAnnotation;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.remote.control.commands.localization.Localization;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public class LocalizedAnnotationTest {

    private static Annotation annotation;
    private static Association association;
    private static Localization localization;
    private static LocalizedAnnotation localizedAnnotation;

    @BeforeAll
    public static void init() {
        annotation = new Annotation("Nanomia bijuga", "brian",
                new VideoIndex(Duration.ofMillis(12345L)),
                UUID.randomUUID());
        annotation.setDuration(Duration.ofMillis(12));
        association = new Association(BoundingBox.LINK_NAME,
                Association.VALUE_SELF,
                """
                        {"x": 10, "y":20, "width": 30, "height": 40}""",
                "application/json",
                UUID.randomUUID());
        annotation.setAssociations(List.of(association));

        localization = new Localization(association.getUuid(),
                annotation.getConcept(),
                annotation.getElapsedTime().toMillis(),
                annotation.getDuration().toMillis(),
                10, 20, 30, 40, "#123456");

        localizedAnnotation = new LocalizedAnnotation(annotation, association);
    }

    private void check(Localization x) {
        assertEquals(annotation.getConcept(), x.getConcept());
        assertEquals(annotation.getElapsedTime().toMillis(), x.getElapsedTimeMillis().longValue());
        assertEquals(annotation.getDuration().toMillis(), x.getDurationMillis().longValue());
        assertEquals(association.getUuid(), x.getUuid());
        assertEquals(localization.getX(), x.getX());
        assertEquals(localization.getY(), x.getY());
        assertEquals(localization.getWidth(), x.getWidth());
        assertEquals(localization.getHeight(), x.getHeight());
    }


//    @Test
//    public void toLocalizationTest() {
//        var opt = localizedAnnotation.toLocalization();
//        assertTrue(opt.isPresent());
//        var x = opt.get();
//        check(x);
//    }

    @Test
    public void fromLocalizationOfNullsTest() {
        var loc = new Localization(UUID.randomUUID(), null, null, null, 0, 0, 0, 0, null);
        var la = LocalizedAnnotation.from(loc);
        var anno = la.annotation();
        var ass = la.association();
        assertEquals(loc.getUuid(), ass.getUuid());
    }


//    @Test
//    public void fromAnnotationTest() {
//        var xs = LocalizedAnnotation.from(annotation);
//        assertEquals(1, xs.size());
//        var la = xs.get(0);
//        assertEquals(annotation, la.annotation());
//        assertEquals(association, la.association());
//        var opt = la.toLocalization();
//        assertTrue(opt.isPresent());
//        var x = opt.get();
//        check(x);
//    }

//    @Test
//    public void fromAnnotationsTest() {
//        var xs = LocalizedAnnotation.from(List.of(annotation));
//        assertEquals(1, xs.size());
//        var la = xs.get(0);
//        assertEquals(annotation, la.annotation());
//        assertEquals(association, la.association());
//        var opt = la.toLocalization();
//        assertTrue(opt.isPresent());
//        var x = opt.get();
//        check(x);
//    }

//    @Test
//    public void fromLocalizationTest() {
//        var la = LocalizedAnnotation.from(localization);
//        var opt = la.toLocalization();
//        assertTrue(opt.isPresent());
//        var x = opt.get();
//        check(x);
//        var anno = la.annotation();
//        assertEquals(annotation.getDuration(), anno.getDuration());
//        assertEquals(annotation.getElapsedTime(), anno.getElapsedTime());
//        assertNull(anno.getObservationUuid());
//        assertEquals(annotation.getConcept(), anno.getConcept());
//
//        var ass = la.association();
//        assertEquals(association.getLinkName(), ass.getLinkName());
//        assertEquals(association.getToConcept(), ass.getToConcept());
//        assertEquals("{\"x\":10,\"y\":20,\"width\":30,\"height\":40,\"generator\":\"VARS Annotation\"}", ass.getLinkValue());
//        assertEquals(association.getMimeType(), ass.getMimeType());
//        assertEquals(association.getUuid(), ass.getUuid());
//
//        var xs = anno.getAssociations();
//        assertEquals(1, xs.size());
//        var ass2 = xs.get(0);
//        assertEquals(ass, ass2);
//
//    }
}
