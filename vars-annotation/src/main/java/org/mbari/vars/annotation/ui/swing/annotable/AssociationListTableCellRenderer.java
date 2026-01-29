package org.mbari.vars.annotation.ui.swing.annotable;

import mbarix4j.swing.ListListModel;
import mbarix4j.swing.table.ListTableCellRenderer;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annosaurus.sdk.r1.models.Association;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AssociationListTableCellRenderer extends ListTableCellRenderer {

    public AssociationListTableCellRenderer() {
        super();
        setPrototypeCellValue("0123456789012345678901234567890"); // required for resize to work
        var cellRenderer = new AssociationListCellRenderer();
        cellRenderer.setOpaque(false);
        cellRenderer.setForeground(Colors.DEFAULT_TABLE_TEXT.getColor());
        cellRenderer.setBackground(Colors.DEFAULT.getColor());
        setCellRenderer(cellRenderer);
        setForeground(Colors.DEFAULT_TABLE_TEXT.getColor());
        setBackground(Colors.DEFAULT.getColor());
        ToolTipManager.sharedInstance().registerComponent(this);
    }


    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        final var component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        final var listModel = getModel();
        final int fixedCellHeight = getFixedCellHeight();
        final int numItemsInList = Math.max(1, listModel.getSize());
        final int cellHeight = Math.max(24, fixedCellHeight * numItemsInList);
        final Dimension preferredSize = component.getPreferredSize();
        if (cellHeight != table.getRowHeight(row)) {
            preferredSize.setSize(preferredSize.getWidth(), cellHeight);
            component.setPreferredSize(preferredSize);
            table.setRowHeight(row, cellHeight);
        }
        return component;
    }

    /**
     * Sets the string for the cell being rendered to <code>value</code>.
     * Overrides the super class to avoid Problems with shared renderers.
     *
     * @param  value the <code>List</code> for this cell; if value is <code>null</code> it sets the value to an empty string
     * @see  JLabel#setText
     */
    @Override
    protected void setValue(final Object value) {
        // Sort them in the view
        final Annotation annotation = (Annotation) value;
        if (annotation != null) {
            var associations = new ArrayList<>(annotation.getAssociations());
            associations.sort(Association.ALPHABETICAL_COMPARATOR);
            final ListModel<Association> listModel = (ListModel<Association>) new ListListModel(associations);
            setModel(listModel);
        }
        else {
            setModel(null);
        }
    }
}
