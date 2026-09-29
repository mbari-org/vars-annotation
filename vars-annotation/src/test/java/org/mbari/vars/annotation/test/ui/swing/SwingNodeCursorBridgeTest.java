package org.mbari.vars.annotation.test.ui.swing;

import javafx.scene.Cursor;
import org.junit.jupiter.api.Test;
import org.mbari.vars.annotation.ui.swing.SwingNodeCursorBridge;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SwingNodeCursorBridgeTest {

    @Test
    public void mapsAwtCursorsToJavaFxCursors() {
        assertEquals(Cursor.H_RESIZE, SwingNodeCursorBridge.toFxCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.E_RESIZE_CURSOR)));
        assertEquals(Cursor.H_RESIZE, SwingNodeCursorBridge.toFxCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.W_RESIZE_CURSOR)));
        assertEquals(Cursor.V_RESIZE, SwingNodeCursorBridge.toFxCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.N_RESIZE_CURSOR)));
        assertEquals(Cursor.HAND, SwingNodeCursorBridge.toFxCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR)));
        assertEquals(Cursor.TEXT, SwingNodeCursorBridge.toFxCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.TEXT_CURSOR)));
        assertEquals(Cursor.DEFAULT, SwingNodeCursorBridge.toFxCursor(java.awt.Cursor.getDefaultCursor()));
        assertEquals(Cursor.DEFAULT, SwingNodeCursorBridge.toFxCursor(null));
    }

    private static void flushEdt() throws Exception {
        SwingUtilities.invokeAndWait(() -> { });
    }

    /** Sends a mouse event to the component and waits for the bridge (which reads the cursor later on the EDT) */
    private static void send(java.awt.Component c, int id, int x) throws Exception {
        SwingUtilities.invokeAndWait(() ->
                c.dispatchEvent(new MouseEvent(c, id, System.currentTimeMillis(), 0, x, 10, 0, false)));
        flushEdt();
    }

    @Test
    public void headerResizeCursorIsForwardedAndResetOnceTheMouseLeavesTheBorder() throws Exception {
        var table = new JTable(new DefaultTableModel(new Object[][]{{"a", "b"}}, new Object[]{"one", "two"}));
        var header = table.getTableHeader();
        var seen = new ArrayList<Cursor>();
        SwingNodeCursorBridge.install(header, seen::add);

        SwingUtilities.invokeAndWait(() -> header.setSize(header.getPreferredSize())); // so it can hit-test columns
        int border = table.getColumnModel().getColumn(0).getWidth() - 1;

        send(header, MouseEvent.MOUSE_ENTERED, border);
        send(header, MouseEvent.MOUSE_MOVED, border);
        assertEquals(Cursor.H_RESIZE, seen.getLast());

        send(header, MouseEvent.MOUSE_MOVED, 20);
        assertEquals(Cursor.DEFAULT, seen.getLast());

        send(header, MouseEvent.MOUSE_MOVED, border);
        send(header, MouseEvent.MOUSE_EXITED, border);
        assertEquals(Cursor.DEFAULT, seen.getLast());
    }

    @Test
    public void onlyReportsChanges() throws Exception {
        var table = new JTable(new DefaultTableModel(new Object[][]{{"a", "b"}}, new Object[]{"one", "two"}));
        var header = table.getTableHeader();
        var seen = new ArrayList<Cursor>();
        SwingNodeCursorBridge.install(header, seen::add);
        SwingUtilities.invokeAndWait(() -> header.setSize(header.getPreferredSize()));

        for (int i = 0; i < 5; i++) {
            send(header, MouseEvent.MOUSE_MOVED, 20 + i);
        }
        assertEquals(List.of(), seen.stream().filter(c -> c != Cursor.DEFAULT).toList());
        assertEquals(true, seen.size() <= 1, "repeated identical cursors should not be re-sent");
    }
}
