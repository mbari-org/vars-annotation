package org.mbari.vars.annotation.test.ui.swing.annotable;

import org.junit.jupiter.api.Test;
import org.mbari.vars.annotation.ui.swing.annotable.SortArrowHeaderRenderer;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SortArrowHeaderRendererTest {

    private static final Color ARROW = Color.decode("#FFB74D");

    private static JTable newTable() {
        var table = new JTable(new DefaultTableModel(new Object[][]{{"a", "b"}, {"c", "d"}}, new Object[]{"one", "two"}));
        table.setAutoCreateRowSorter(true);
        return table;
    }

    private static TableCellRenderer newRenderer(JTable table) {
        return new SortArrowHeaderRenderer(table.getTableHeader().getDefaultRenderer(), ARROW);
    }

    private static JLabel render(TableCellRenderer renderer, JTable table, int column) {
        return (JLabel) renderer.getTableCellRendererComponent(table, "header", false, false, -1, column);
    }

    /** @return number of pixels with the given color and how many are dark (r,g,b all below 60) */
    private static int[] countPixels(javax.swing.Icon icon, Color color) {
        var img = new BufferedImage(icon.getIconWidth() + 2, icon.getIconHeight() + 2, BufferedImage.TYPE_INT_ARGB);
        var g = img.createGraphics();
        icon.paintIcon(null, g, 1, 1);
        g.dispose();
        int match = 0;
        int dark = 0;
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                int argb = img.getRGB(x, y);
                if ((argb >>> 24) > 200) {
                    var c = new Color(argb, true);
                    if (c.equals(color)) match++;
                    if (c.getRed() < 60 && c.getGreen() < 60 && c.getBlue() < 60) dark++;
                }
            }
        }
        return new int[]{match, dark};
    }

    @Test
    public void sortedColumnsGetAnArrowInTheRequestedColor() {
        var table = newTable();
        var renderer = newRenderer(table);
        table.getRowSorter().setSortKeys(List.of(new RowSorter.SortKey(1, SortOrder.ASCENDING)));

        var icon = render(renderer, table, 1).getIcon();
        assertNotNull(icon);
        var counts = countPixels(icon, ARROW);
        assertTrue(counts[0] > 10, "arrow should be drawn in the requested color");
        assertEquals(0, counts[1], "arrow should not be black");
    }

    @Test
    public void ascendingAndDescendingArePainted() {
        var table = newTable();
        var renderer = newRenderer(table);

        table.getRowSorter().setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
        var ascending = render(renderer, table, 0).getIcon();
        table.getRowSorter().setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.DESCENDING)));
        var descending = render(renderer, table, 0).getIcon();

        assertNotNull(ascending);
        assertNotNull(descending);
        assertTrue(ascending != descending);
        assertTrue(countPixels(descending, ARROW)[0] > 10);
    }

    @Test
    public void onlyThePrimarySortColumnGetsAnArrow() {
        var table = newTable();
        var renderer = newRenderer(table);
        table.getRowSorter().setSortKeys(List.of(new RowSorter.SortKey(1, SortOrder.ASCENDING)));

        var unsorted = render(renderer, table, 0);
        assertNull(unsorted.getIcon());
        assertEquals("header", unsorted.getText());
    }

    @Test
    public void followsTheColumnWhenColumnsAreReordered() {
        var table = newTable();
        var renderer = newRenderer(table);
        table.getRowSorter().setSortKeys(List.of(new RowSorter.SortKey(1, SortOrder.ASCENDING)));
        table.getColumnModel().moveColumn(0, 1); // model column 1 is now in view column 0

        assertNotNull(render(renderer, table, 0).getIcon());
        assertNull(render(renderer, table, 1).getIcon());
    }
}
