package org.mbari.vars.ui.swing.annotable;

import org.jdesktop.swingx.table.DefaultTableColumnModelExt;
import org.jdesktop.swingx.table.TableColumnExt;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.util.FormatUtils;

import java.util.Comparator;
import java.util.ResourceBundle;
import java.util.function.Function;


public class AnnotationTableColumnModel extends DefaultTableColumnModelExt {


    public AnnotationTableColumnModel(ResourceBundle i18n) {
        super();
        init(i18n);
    }

    private void init(ResourceBundle i18n) {
        addColumn(new AnnotationTableColumn(i18n.getString("annotable.col.timestamp"),
                0,
                safely(Annotation::getRecordedTimestamp),
                90,
                Comparator.comparing(Annotation::getRecordedTimestamp)));

        addColumn(new AnnotationTableColumn(i18n.getString("annotable.col.timecode"),
                1,
                safely(Annotation::getTimecode),
                90));

        addColumn(new AnnotationTableColumn(i18n.getString("annotable.col.elapsedtime"),
                2,
                (a) -> {
                    var et = a.getElapsedTime();
                    if (et != null) {
                        return FormatUtils.formatDuration(et);
                    }
                    else {
                        return null;
                    }
                },
                45,
                Comparator.comparing(Annotation::getElapsedTime)));

        addColumn(new AnnotationTableColumn(i18n.getString("annotable.col.concept"),
                3,
                Annotation::getConcept,
                140));

        var associationCol = new TableColumnExt(4, 140,  new AssociationListTableCellRenderer(), null);
        var associationId = i18n.getString("annotable.col.association");
        associationCol.setEditable(false);
        associationCol.setIdentifier(associationId);
        associationCol.setHeaderValue(associationId);
        associationCol.setSortable(false);
        addColumn(associationCol);

        var fgsCol = new TableColumnExt(5, 45, new FGSCellRenderer(), null);
        var fgsId = i18n.getString("annotable.col.framegrab");
        fgsCol.setEditable(false);
        fgsCol.setIdentifier(fgsId);
        fgsCol.setHeaderValue(fgsId);
        fgsCol.setSortable(false);
        addColumn(fgsCol);

        addColumn(new AnnotationTableColumn(i18n.getString("annotable.col.observer"),
                6,
                Annotation::getObserver,
                50));

        addColumn(new AnnotationTableColumn(i18n.getString("annotable.col.duration"),
                7,
                (a) -> {
                    var d = a.getDuration();
                    if (d != null) {
                        return FormatUtils.formatDuration(d);
                    }
                    else {
                        return null;
                    }
                },
                45,
                Comparator.comparing(Annotation::getDuration)));

        addColumn(new AnnotationTableColumn(i18n.getString("annotable.col.activity"),
                8,
                Annotation::getActivity,
                50));

        addColumn(new AnnotationTableColumn(i18n.getString("annotable.col.group"),
                9,
                Annotation::getGroup,
                50));

    }

    private Function<Annotation, String> safely(Function<Annotation, ?> fn) {
        return (a) -> {
            try {
                var v = fn.apply(a);
                if (v != null) {
                    return v.toString();
                }
                else {
                    return null;
                }
            }
            catch (Exception e) {
                return null;
            }
        };
    }

}
