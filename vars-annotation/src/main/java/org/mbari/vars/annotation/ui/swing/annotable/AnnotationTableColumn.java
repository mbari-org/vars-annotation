package org.mbari.vars.annotation.ui.swing.annotable;

import org.jdesktop.swingx.table.TableColumnExt;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;
import java.util.Comparator;
import java.util.function.Function;

public class AnnotationTableColumn extends TableColumnExt {


    public AnnotationTableColumn(String id,
                                int modelIndex,
                                 Function<Annotation, String> converter,
                                 int width) {
        this(id, modelIndex, converter, width,  Comparator.comparing(converter));
    }

    public AnnotationTableColumn(String id,
                                 int modelIndex,
                                 Function<Annotation, String> converter,
                                 int width,
                                 Comparator<Annotation> comparator) {
        super(modelIndex, width, new DefaultTableCellRenderer(), new DefaultCellEditor(new JTextField()));
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
        setVisible(true);
        setComparator(comparator);
        setPreferredWidth(100);
    }

}
