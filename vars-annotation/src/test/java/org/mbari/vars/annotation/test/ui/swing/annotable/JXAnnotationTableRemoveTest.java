package org.mbari.vars.annotation.test.ui.swing.annotable;

import org.junit.jupiter.api.Test;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annotation.ui.Initializer;
import org.mbari.vars.annotation.ui.events.AnnotationsAddedEvent;
import org.mbari.vars.annotation.ui.events.AnnotationsRemovedEvent;
import org.mbari.vars.annotation.ui.swing.annotable.JXAnnotationTableController;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JXAnnotationTableRemoveTest {

    private static Annotation anno(String concept) {
        var a = new Annotation();
        a.setObservationUuid(UUID.randomUUID());
        a.setConcept(concept);
        return a;
    }

    private static void flushEdt() throws Exception {
        SwingUtilities.invokeAndWait(() -> { });
        SwingUtilities.invokeAndWait(() -> { });
    }

    /**
     * The bulk editor deletes annotations from a whole deployment, but the main table only shows the
     * annotations of the open video. Removing something that isn't in the table must not stop the
     * things that are in it from being removed.
     */
    @Test
    public void removingAnnotationsThatAreNotInTheTableStillRemovesTheOnesThatAre() throws Exception {
        var toolBox = Initializer.getToolBox();
        var controller = new JXAnnotationTableController(toolBox);
        var inTable = List.of(anno("a"), anno("b"), anno("c"));
        var notInTable = anno("elsewhere");

        var holder = new Object[1];
        SwingUtilities.invokeAndWait(() -> holder[0] = controller.getTable());
        var table = (org.jdesktop.swingx.JXTable) holder[0];

        toolBox.getEventBus().send(new AnnotationsAddedEvent(null, inTable));
        flushEdt();
        assertEquals(3, table.getRowCount());

        toolBox.getEventBus().send(new AnnotationsRemovedEvent(null, List.of(notInTable, inTable.get(1))));
        flushEdt();
        assertEquals(2, table.getRowCount());
    }

    @Test
    public void removingOnlyAnnotationsThatAreNotInTheTableChangesNothing() throws Exception {
        var toolBox = Initializer.getToolBox();
        var controller = new JXAnnotationTableController(toolBox);
        var holder = new Object[1];
        SwingUtilities.invokeAndWait(() -> holder[0] = controller.getTable());
        var table = (org.jdesktop.swingx.JXTable) holder[0];

        toolBox.getEventBus().send(new AnnotationsAddedEvent(null, List.of(anno("a"), anno("b"))));
        flushEdt();
        toolBox.getEventBus().send(new AnnotationsRemovedEvent(null, List.of(anno("x"), anno("y"))));
        flushEdt();
        assertEquals(2, table.getRowCount());
    }
}
