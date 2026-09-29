package org.mbari.vars.annotation.test.ui.javafx.deployeditor;

import org.junit.jupiter.api.Test;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annotation.etc.rxjava.EventBus;
import org.mbari.vars.annotation.ui.commands.ChangeConceptCmd;
import org.mbari.vars.annotation.ui.commands.ChangeGroupCmd;
import org.mbari.vars.annotation.ui.commands.Command;
import org.mbari.vars.annotation.ui.commands.DeleteAnnotationsCmd;
import org.mbari.vars.annotation.ui.events.AnnotationsSelectedEvent;
import org.mbari.vars.annotation.ui.javafx.deployeditor.AnnotationTableController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * The bulk editor sends its commands on its own event bus. Only commands that get forwarded to
 * the main event bus are run by the CommandManager. A command that isn't forwarded silently does nothing.
 */
public class CommandForwardingTest {

    private static List<Annotation> annotations() {
        var a = new Annotation();
        a.setObservationUuid(UUID.randomUUID());
        a.setConcept("object");
        return List.of(a);
    }

    private static List<Object> forwardedWhenSending(Object... toSend) {
        var local = new EventBus();
        var main = new EventBus();
        var received = new ArrayList<Object>();
        main.toObserverable().subscribe(received::add);
        AnnotationTableController.forwardCommands(local, main);
        for (var o : toSend) {
            local.send(o);
        }
        return received;
    }

    @Test
    public void deleteAnnotationsIsForwarded() {
        var cmd = new DeleteAnnotationsCmd(annotations());
        var received = forwardedWhenSending(cmd);
        assertEquals(1, received.size());
        assertSame(cmd, received.getFirst());
    }

    @Test
    public void otherBulkEditorCommandsAreStillForwarded() {
        var rename = new ChangeConceptCmd(annotations(), "object");
        var regroup = new ChangeGroupCmd(annotations(), "ROV");
        assertEquals(List.of(rename, regroup), forwardedWhenSending(rename, regroup));
    }

    @Test
    public void everyKindOfCommandIsForwarded() {
        // e.g. a command nobody remembered to add to a list
        Command custom = new Command() {
            @Override public void apply(org.mbari.vars.annotation.ui.UIToolBox toolBox) { }
            @Override public void unapply(org.mbari.vars.annotation.ui.UIToolBox toolBox) { }
            @Override public String getDescription() { return "custom"; }
        };
        assertEquals(List.of(custom), forwardedWhenSending(custom));
    }

    @Test
    public void nonCommandsAreNotForwarded() {
        var selected = new AnnotationsSelectedEvent(annotations());
        assertEquals(List.of(), forwardedWhenSending(selected, "hello"));
    }
}
