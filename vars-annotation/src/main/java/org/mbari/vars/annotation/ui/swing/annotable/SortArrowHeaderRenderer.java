package org.mbari.vars.annotation.ui.swing.annotable;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

/**
 * Wraps a table's normal header renderer so the sort indicator is drawn in a color we choose. The
 * platform's default indicator is a tiny black triangle, which is nearly invisible on our dark
 * header. Everything else about the header (text, borders, background) is left to the wrapped renderer.
 */
public class SortArrowHeaderRenderer implements TableCellRenderer {

    private final TableCellRenderer delegate;
    private final Icon ascendingIcon;
    private final Icon descendingIcon;

    public SortArrowHeaderRenderer(TableCellRenderer delegate, Color arrowColor) {
        this.delegate = delegate;
        this.ascendingIcon = new ArrowIcon(SortOrder.ASCENDING, arrowColor);
        this.descendingIcon = new ArrowIcon(SortOrder.DESCENDING, arrowColor);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column) {
        Component c = delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (c instanceof JLabel label) {
            var sortOrder = sortOrder(table, column);
            switch (sortOrder) {
                case ASCENDING -> label.setIcon(ascendingIcon);
                case DESCENDING -> label.setIcon(descendingIcon);
                default -> { } // Not sorted: leave whatever the delegate set
            }
            if (sortOrder != SortOrder.UNSORTED) {
                label.setHorizontalTextPosition(SwingConstants.LEADING);
            }
        }
        return c;
    }

    /**
     * @param column The column's index in the view (as passed to a renderer)
     * @return The sort order of the column, considering only the primary sort key
     */
    static SortOrder sortOrder(JTable table, int column) {
        if (table == null || column < 0) {
            return SortOrder.UNSORTED;
        }
        RowSorter<?> sorter = table.getRowSorter();
        if (sorter == null || sorter.getSortKeys().isEmpty()) {
            return SortOrder.UNSORTED;
        }
        var primary = sorter.getSortKeys().getFirst();
        return primary.getColumn() == table.convertColumnIndexToModel(column)
                ? primary.getSortOrder()
                : SortOrder.UNSORTED;
    }

    /** A small filled triangle: points up for ascending, down for descending. */
    static class ArrowIcon implements Icon {
        private static final int WIDTH = 11;
        private static final int HEIGHT = 7;
        private final SortOrder sortOrder;
        private final Color color;

        ArrowIcon(SortOrder sortOrder, Color color) {
            this.sortOrder = sortOrder;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            var g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                var triangle = new Path2D.Double();
                if (sortOrder == SortOrder.ASCENDING) {
                    triangle.moveTo(x, y + HEIGHT);
                    triangle.lineTo(x + WIDTH, y + HEIGHT);
                    triangle.lineTo(x + WIDTH / 2.0, y);
                }
                else {
                    triangle.moveTo(x, y);
                    triangle.lineTo(x + WIDTH, y);
                    triangle.lineTo(x + WIDTH / 2.0, y + HEIGHT);
                }
                triangle.closePath();
                g2.fill(triangle);
            }
            finally {
                g2.dispose();
            }
        }

        @Override
        public int getIconWidth() {
            return WIDTH;
        }

        @Override
        public int getIconHeight() {
            return HEIGHT;
        }
    }
}
