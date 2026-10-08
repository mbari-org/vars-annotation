package org.mbari.vars.annotation.test.ui.swing.annotable;

import org.jdesktop.swingx.JXTable;
import org.junit.jupiter.api.Test;
import org.mbari.vars.annotation.ui.Initializer;
import org.mbari.vars.annotation.ui.swing.annotable.JXAnnotationTableController;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JXAnnotationTableResizeTest {

    private static void send(Component c, int id, int x) throws Exception {
        SwingUtilities.invokeAndWait(() -> c.dispatchEvent(new MouseEvent(c, id, System.currentTimeMillis(),
                InputEvent.BUTTON1_DOWN_MASK, x, 5, 1, false, MouseEvent.BUTTON1)));
    }

    /** Drags the right border of a column by dx pixels */
    private static void dragBorder(JXTable table, int column, int dx) throws Exception {
        var header = table.getTableHeader();
        int[] border = new int[1];
        SwingUtilities.invokeAndWait(() -> border[0] = header.getHeaderRect(column).x
                + header.getHeaderRect(column).width - 1);
        send(header, MouseEvent.MOUSE_MOVED, border[0]);
        send(header, MouseEvent.MOUSE_PRESSED, border[0]);
        for (int d = 5; d <= dx; d += 5) {
            send(header, MouseEvent.MOUSE_DRAGGED, border[0] + d);
        }
        send(header, MouseEvent.MOUSE_RELEASED, border[0] + dx);
        SwingUtilities.invokeAndWait(() -> SwingUtilities.getWindowAncestor(table).validate());
    }

    private static int width(JXTable table, int column) throws Exception {
        int[] w = new int[1];
        SwingUtilities.invokeAndWait(() -> w[0] = table.getColumnModel().getColumn(column).getWidth());
        return w[0];
    }

    /**
     * When the table was narrower than its columns, they were all squeezed to their minimum width
     * and none of them could be resized (vars-annotation#162).
     */
    @Test
    public void columnsCanBeResizedInANarrowTable() throws Exception {
        var controller = new JXAnnotationTableController(Initializer.getToolBox());
        var holder = new Object[2];
        SwingUtilities.invokeAndWait(() -> {
            var table = controller.getTable();
            var scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(230, 200));
            var frame = new JFrame();
            frame.add(scrollPane);
            frame.pack(); // makes it displayable so it gets laid out, without showing it
            holder[0] = table;
            holder[1] = frame;
        });
        var table = (JXTable) holder[0];
        try {
            for (int column = 0; column < 2; column++) {
                int before = width(table, column);
                int next = width(table, column + 1);
                dragBorder(table, column, 40);
                assertEquals(before + 40, width(table, column), "column " + column);
                assertEquals(next, width(table, column + 1), "column " + (column + 1) + " should not be squeezed");
            }
        }
        finally {
            SwingUtilities.invokeAndWait(() -> ((JFrame) holder[1]).dispose());
        }
    }
}
