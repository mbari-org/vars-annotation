package org.mbari.vars.annotation.ui.swing.annotable;

import org.mbari.vars.annosaurus.sdk.r1.models.Association;

import javax.swing.*;
import java.awt.*;

public class AssociationListCellRenderer extends DefaultListCellRenderer {

    private String associationAsString;

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        final Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof String s) {
            associationAsString = s;
        }
        else if (value instanceof Association a) {
            associationAsString = a.toString();
        }
        else {
            associationAsString = "--No Association--";
        }
        setText(associationAsString);
        return component;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension dim = super.getPreferredSize();
        if (dim != null) {
            dim = new Dimension(dim.width + 4, dim.height);
        }

        return dim;
    }


    // Override the default to send back the ConceptName string

    /**
     *  Gets the toolTipText attribute of the AssociationListCellRenderer object
     *
     * @return  The toolTipText value
     */
    @Override
    public String getToolTipText() {
        return associationAsString;
    }
}
