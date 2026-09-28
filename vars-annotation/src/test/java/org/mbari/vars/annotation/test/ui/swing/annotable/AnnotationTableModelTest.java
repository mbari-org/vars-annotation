package org.mbari.vars.annotation.test.ui.swing.annotable;

import org.junit.jupiter.api.Test;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annotation.ui.swing.annotable.AnnotationTableModel;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnnotationTableModelTest {

    private static Annotation anno(UUID uuid, String concept) {
        var a = new Annotation();
        a.setObservationUuid(uuid);
        a.setConcept(concept);
        return a;
    }

    @Test
    public void updateAndRemoveOfUnknownAnnotationFiresNoEvents() {
        var model = new AnnotationTableModel(ResourceBundle.getBundle("i18n"));
        var known = anno(UUID.randomUUID(), "a");
        model.addAnnotation(known);

        var events = new ArrayList<Integer>();
        model.addTableModelListener(e -> events.add(e.getFirstRow()));

        var unknown = anno(UUID.randomUUID(), "b");
        model.updateAnnotation(unknown);
        model.removeAnnotation(unknown);
        assertEquals(List.of(), events);
        assertEquals(1, model.getRowCount());

        model.updateAnnotation(anno(known.getObservationUuid(), "c"));
        assertEquals(List.of(0), events);
        assertEquals("c", model.getAnnotationAt(0).getConcept());
    }
}
