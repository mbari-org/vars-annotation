package org.mbari.m3.vars.annotation.ui.shared;

import java.util.LinkedHashSet;


import javafx.collections.ObservableList;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Callback;

/**
 * Extra calls for TableView which would have been nice to see in JavaFx TableView
 */
public class TableViewExt<T> {// com.sun.javafx: implements ChangeListener<Skin<?>>

    private final TableView<T> tableView;
    private LinkedHashSet<TableRow<T>> rows = new LinkedHashSet<>();
    private int firstIndex;
    private int lastIndex;

    public TableViewExt(TableView<T> tableView) {
        this.tableView = tableView;

        // Callback to monitor row creation and to identify visible screen rows
        final Callback<TableView<T>, TableRow<T>> rf = tableView.getRowFactory();

        final Callback<TableView<T>, TableRow<T>> modifiedRowFactory = param -> {
            TableRow<T> r = rf != null ? rf.call(param) : new TableRow<T>();
            // Save row, this implementation relies on JaxaFX re-using TableRow efficiently
            rows.add(r);
            return r;
        };
        tableView.setRowFactory(modifiedRowFactory);
    }


    /**
     * Changes the current view to ensure that one of the passed index positions
     * is visible on screen. The view is not changed if any of the passed index positions is already visible.
     * The table scroll position is moved so that the closest index to the current position is visible.
     * @param indices Assumed to be in ascending order.
     *
     */
    public void
    scrollToIndex(int ... indices) {
        int first = getFirstVisibleIndex();
        int last = getLastVisibleIndex();
        int where = first;

        boolean changeScrollPos = true;

        // No point moving current scroll position if one of the index items is visible already:
        if (first >= 0 && last >= first) {
            for (int idx : indices) {
                if (first <= idx && idx <= last) {
                    changeScrollPos = false;
                    break;
                }
            }
        }


        if (indices.length > 0 && changeScrollPos) {
            where = indices[0];
            if (first >= 0) {
                int x = closestTo(indices, first);
                int abs = Math.abs(x - first);
                if (abs < Math.abs(where - first))
                {
                    where = x;
                }
            }
            if (last >= 0) {
                int x = closestTo(indices, last);
                int abs = Math.abs(x - last);
                if (abs < Math.abs(where - last))
                {
                    where = x;
                }
            }
            tableView.scrollTo(where);
        }
    }

    private static int closestTo(int[] indices, int value) {
        int x    = indices[0];
        int diff = Math.abs(value - x);
        int newDiff = diff;
        for (int v : indices) {
            newDiff = Math.abs(value - v);
            if (newDiff < diff) {
                x    = v;
                diff = newDiff;
            }
        }
        return x;
    }

    private void recomputeVisibleIndexes() {
        firstIndex = -1;
        lastIndex = -1;

        // Work out which of the rows are visible
        double tblViewHeight = tableView.getHeight();
        double headerHeight = tableView.lookup(".column-header-background").getBoundsInLocal().getHeight();
        double viewPortHeight = tblViewHeight - headerHeight;
        for(TableRow<T> r : rows) {
            double minY = r.getBoundsInParent().getMinY();
            double maxY = r.getBoundsInParent().getMaxY();

            boolean hidden  = (maxY < 0) || (minY > viewPortHeight);
            // boolean fullyVisible = !hidden && (maxY <= viewPortHeight) && (minY >= 0);
            if (!hidden) {
                if (firstIndex < 0 || r.getIndex() < firstIndex) {
                    firstIndex = r.getIndex();
                }
                if (lastIndex < 0 || r.getIndex() > lastIndex) {
                    lastIndex = r.getIndex();
                }
            }
        }
    }
    /**
     * Find the first row in the tableView which is visible on the display
     * @return -1 if none visible or the index of the first visible row (wholly or fully)
     */
    public int getFirstVisibleIndex() {
        recomputeVisibleIndexes();
        //System.out.println("getFirstVisibleIndex "+firstIndex+" rows="+rows.size()/*com.sun.javafx:+" jre8Index="+jre8Index*/);
        return firstIndex;
    }

    /**
     * Find the last row in the tableView which is visible on the display
     * @return -1 if none visible or the index of the last visible row (wholly or fully)
     */
    public int getLastVisibleIndex() {
        recomputeVisibleIndexes();
        //System.out.println("getLastVisibleIndex "+lastIndex+" rows="+rows.size()/*com.sun.javafx:+" jre8Index="+jre8Index*/);
        return lastIndex;
    }

    public int[] getVisibleRows() {
        recomputeVisibleIndexes();
        return new int[]{firstIndex, lastIndex};
    }

    /**
     * Ensure that some part of the current selection is visible in the display view
     */
    public void scrollToSelection() {
        ObservableList<Integer> selection = tableView.getSelectionModel().getSelectedIndices();
        int[] indices = new int[selection.size()];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = selection.get(i);
        }
        scrollToIndex(indices);
    }
}