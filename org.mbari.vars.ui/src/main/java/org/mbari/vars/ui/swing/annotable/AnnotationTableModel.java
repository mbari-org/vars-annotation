package org.mbari.vars.ui.swing.annotable;

import org.mbari.vars.services.model.Annotation;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

public class AnnotationTableModel extends AbstractTableModel {

    private final TableColumnModel tableColumnModel;
    private final List<Annotation> annotations = new ArrayList<>();

    public AnnotationTableModel(ResourceBundle i18n) {
        this.tableColumnModel = new AnnotationTableColumnModel(i18n);
    }

    public void addAnnotation(Annotation a) {
        annotations.add(a);
        int i = annotations.size() - 1;
        fireTableRowsInserted(i, i);
    }

    public void updateAnnotation(Annotation a) {
        int i = annotations.indexOf(a);
        if (i > -1) {
            annotations.set(i, a);
        }
        fireTableRowsUpdated(i, i);
    }

    public void removeAnnotation(Annotation a) {
        int i = annotations.indexOf(a);
        if (i > -1) {
            annotations.remove(i);
        }
        fireTableRowsDeleted(i, i);
    }

    public void addAnnotations(Collection<Annotation> xs) {
        int i = annotations.size();
        int j = xs.size() + i - 1;
        annotations.addAll(xs);
        fireTableRowsInserted(i, j);
    }


    public void clear() {
        int n = annotations.size() - 1;
        annotations.clear();
        fireTableRowsDeleted(0, n);
    }

    @Override
    public int getRowCount() {
        return annotations.size();
    }

    @Override
    public int getColumnCount() {
        return tableColumnModel.getColumnCount();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return getAnnotationAt(rowIndex);
    }

    public Annotation getAnnotationAt(int rowIndex) {
        if (rowIndex < annotations.size() && rowIndex > -1) {
            return annotations.get(rowIndex);
        }
        else {
            return null;
        }
    }

    public int getAnnotationRow(Annotation a) {
        return annotations.indexOf(a);
    }

    public TableColumnModel getTableColumnModel() {
        return tableColumnModel;
    }
}
