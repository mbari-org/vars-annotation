package org.mbari.m3.vars.annotation.ui.annotable;

import javafx.collections.FXCollections;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Association;

import java.util.List;

/**
 * @author Brian Schlining
 * @since 2017-05-22T16:20:00
 */
public class AssociationsTableCell extends TableCell<Annotation, List<Association>> {

    private ListView<Association> listView = new ListView<>();

    public AssociationsTableCell() {
        getStyleClass().add("annotable-association-cell");
    }

    @Override
    protected void updateItem(List<Association> item, boolean empty) {
        super.updateItem(item, empty);
        if (item  == null || empty) {
            setText(null);
            listView.setItems(FXCollections.emptyObservableList());
            setGraphic(listView);
        }
        else {
            setText(null);
            listView.setItems(FXCollections.observableArrayList(item));
            setGraphic(listView);
        }
    }
}
