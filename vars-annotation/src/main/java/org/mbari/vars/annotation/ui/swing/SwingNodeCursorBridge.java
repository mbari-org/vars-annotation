package org.mbari.vars.annotation.ui.swing;

import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.scene.Cursor;

import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * A {@link SwingNode} doesn't pass the mouse cursor of the Swing components it hosts on to JavaFX.
 * A Swing table header sets a resize cursor over a column border but the JavaFX window keeps showing
 * the default arrow. This mirrors a Swing component's cursor onto the JavaFX side.
 */
public final class SwingNodeCursorBridge {

    private SwingNodeCursorBridge() {
        // no instantiation
    }

    /**
     * Keeps the cursor of a {@link SwingNode} in sync with a component that it displays.
     *
     * @param component The Swing component whose cursor should be mirrored (e.g. a table header)
     * @param node The node hosting the component
     */
    public static void install(Component component, SwingNode node) {
        install(component, cursor -> Platform.runLater(() -> node.setCursor(cursor)));
    }

    /**
     * @param component The Swing component whose cursor should be mirrored
     * @param onCursor Called, on the Swing event thread, each time the component's cursor changes. Only
     *                 changes are reported.
     */
    public static void install(Component component, Consumer<Cursor> onCursor) {
        var listener = new MouseAdapter() {
            private Cursor last = Cursor.DEFAULT;

            private void update(boolean mouseIsOutside) {
                // The component's own UI listens to the same events and sets the AWT cursor. Wait until
                // all the listeners have run before we look at it.
                SwingUtilities.invokeLater(() -> {
                    // Swing never resets a component's cursor when the mouse leaves it, the OS just
                    // shows the next component's cursor. We have to do that part ourselves.
                    var cursor = mouseIsOutside ? Cursor.DEFAULT : toFxCursor(component.getCursor());
                    if (cursor != last) {
                        last = cursor;
                        onCursor.accept(cursor);
                    }
                });
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                update(false);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                update(false);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                update(false);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                update(true);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                update(false);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                update(false);
            }
        };
        component.addMouseListener(listener);
        component.addMouseMotionListener(listener);
    }

    public static Cursor toFxCursor(java.awt.Cursor cursor) {
        if (cursor == null) {
            return Cursor.DEFAULT;
        }
        return switch (cursor.getType()) {
            case java.awt.Cursor.E_RESIZE_CURSOR, java.awt.Cursor.W_RESIZE_CURSOR -> Cursor.H_RESIZE;
            case java.awt.Cursor.N_RESIZE_CURSOR, java.awt.Cursor.S_RESIZE_CURSOR -> Cursor.V_RESIZE;
            case java.awt.Cursor.NE_RESIZE_CURSOR, java.awt.Cursor.SW_RESIZE_CURSOR -> Cursor.NE_RESIZE;
            case java.awt.Cursor.NW_RESIZE_CURSOR, java.awt.Cursor.SE_RESIZE_CURSOR -> Cursor.NW_RESIZE;
            case java.awt.Cursor.HAND_CURSOR -> Cursor.HAND;
            case java.awt.Cursor.TEXT_CURSOR -> Cursor.TEXT;
            case java.awt.Cursor.WAIT_CURSOR -> Cursor.WAIT;
            case java.awt.Cursor.CROSSHAIR_CURSOR -> Cursor.CROSSHAIR;
            case java.awt.Cursor.MOVE_CURSOR -> Cursor.MOVE;
            default -> Cursor.DEFAULT;
        };
    }
}
