package org.mbari.m3.vars.annotation.ui.annotable;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
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
        listView.setEditable(false);
        // Disable selection
        listView.getSelectionModel()
                .selectedIndexProperty()
                .addListener((obs, oldv, newv) ->
                        Platform.runLater(() -> listView.getSelectionModel().clearSelection()));
    }

    public ListView<Association> getListView() {
        return listView;
    }

    @Override
    protected void updateItem(List<Association> item, boolean empty) {
        super.updateItem(item, empty);
        if (item  == null || empty) {
            setText(null);
            listView.setItems(FXCollections.emptyObservableList());
            listView.setPrefSize(listView.getWidth(), 0);
            setGraphic(null);
        }
        else {
            setText(null);
            listView.setItems(FXCollections.observableArrayList(item));
            listView.setPrefSize(listView.getWidth(), item.size() * 26);
            setGraphic(listView);
        }
    }
}
