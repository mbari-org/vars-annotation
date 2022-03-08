package org.mbari.vars.ui.javafx.annotable;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Brian Schlining
 * @since 2017-05-22T16:20:00
 */
public class AssociationsTableCell extends TableCell<Annotation, List<Association>> {

    private final ListView<Association> listView = new ListView<>();
    private static final Comparator<Association> alphabetical = Comparator.comparing(Association::toString);

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
            var sortedItems = FXCollections.observableArrayList(item);
            sortedItems.sort(alphabetical);
            listView.setItems(sortedItems);
            listView.setPrefSize(listView.getWidth(), item.size() * 26);
            setGraphic(listView);
        }
    }
}
