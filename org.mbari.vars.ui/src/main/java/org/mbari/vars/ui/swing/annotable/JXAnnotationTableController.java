package org.mbari.vars.ui.swing.annotable;

import io.reactivex.Observable;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTableHeader;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.events.AnnotationsAddedEvent;
import org.mbari.vars.ui.events.AnnotationsSelectedEvent;
import org.mbari.vars.ui.util.JFXUtilities;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JXAnnotationTableController {

    private final UIToolBox toolBox;
    private final AnnotationTableModel tableModel;
    private JXTable table;


    public JXAnnotationTableController(UIToolBox toolBox) {
        this.toolBox = toolBox;
        this.tableModel = new AnnotationTableModel(toolBox.getI18nBundle());
        init();
    }

    private void init() {
        var observable = toolBox.getEventBus().toObserverable();
        observable.ofType(AnnotationsAddedEvent.class)
                .subscribe(e -> {
                    var filtered = filterAnnotations(e.get());
                    getTableView().getItems().addAll(filtered);
                    getTableView().sort();
                }));

    }


    private JXTable getTable() {
        if (table == null) {
            var tableColumnModel = tableModel.getTableColumnModel();
            table = new JXTable(tableModel, tableColumnModel);

            var tableHeader = new JXTableHeader(tableColumnModel);
            tableHeader.setReorderingAllowed(true);
            tableHeader.setTable(table);
            table.setTableHeader(tableHeader);

            table.setSortable(true);
            table.setAutoscrolls(true);
            table.setShowGrid(true, false);
            table.setGridColor(Colors.DEFAULT_TEXT.getColor());
            table.setSortOrder(0, SortOrder.ASCENDING); // default sort is recordedTimestamp

            // The row editor panel should get focus NOT the table
            table.setFocusable(false);
            table.getSelectionModel()
                    .addListSelectionListener(e -> {
                        if (!e.getValueIsAdjusting()) {
                            int[] rows = table.getSelectedRows();
                            var selected = new ArrayList<Annotation>(rows.length);
                            for (int i = 0; i < rows.length; i++) {
                                selected.add(getAnnotationAt(i));
                            }
                            toolBox.getEventBus().send(new AnnotationsSelectedEvent(selected));
                        }
                    });
        }
        return table;
    }

    private List<Annotation> filterAnnotations(Collection<Annotation> annotations) {
        var data = toolBox.getData();
        var currentGroupOnly = data.isShowCurrentGroupOnly();
        if (currentGroupOnly) {
            var currentGroup = data.getGroup();
            if (currentGroup != null) {
                return annotations.stream()
                        .filter(a -> a.getGroup() != null && a.getGroup().equalsIgnoreCase(currentGroup))
                        .toList();
            }
        }
        return annotations;
    }

    private void select(Collection<Annotation> annotations) {
        ListSelectionModel selectionModel = getTable().getSelectionModel();
        selectionModel.setValueIsAdjusting(true);
        selectionModel.clearSelection();
        annotations.forEach(a -> {
            final var row = table.convertRowIndexToView(tableModel.getAnnotationRow(a));
            selectionModel.addSelectionInterval(row, row);
        });
        selectionModel.setValueIsAdjusting(false);

        int[] i = table.getSelectedRows();
        if (i != null && i.length > 0) {
            table.scrollRowToVisible(i[0]);
        }
    }

    public Annotation getAnnotationAt(int row) {
        return tableModel.getAnnotationAt(getTable().convertRowIndexToModel(row));
    }
}
