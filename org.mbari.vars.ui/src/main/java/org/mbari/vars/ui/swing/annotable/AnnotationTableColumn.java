package org.mbari.vars.ui.swing.annotable;

import org.jdesktop.swingx.table.TableColumnExt;
import org.mbari.vars.services.model.Annotation;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;
import java.util.function.Function;

public class AnnotationTableColumn extends TableColumnExt {


    public AnnotationTableColumn(String id,
                                int modelIndex,
                                 Function<Annotation, String> converter,
                                 int width) {
        super(modelIndex, width, new DefaultTableCellRenderer(), null);
        setIdentifier(id);
        setHeaderValue(id);
        var renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                final Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Annotation a) {
                    setText(converter.apply(a));
                }
                else {
                    setText(null);
                }
                return component;
            }
        };
        setCellRenderer(renderer);
        setEditable(false);
    }

}
