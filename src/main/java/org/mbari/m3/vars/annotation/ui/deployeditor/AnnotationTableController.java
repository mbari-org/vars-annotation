package org.mbari.m3.vars.annotation.ui.deployeditor;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.events.AnnotationsAddedEvent;
import org.mbari.m3.vars.annotation.events.AnnotationsChangedEvent;
import org.mbari.m3.vars.annotation.events.AnnotationsRemovedEvent;
import org.mbari.m3.vars.annotation.events.MediaChangedEvent;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.m3.vars.annotation.services.CombinedMediaAnnotationDecorator;
import org.mbari.m3.vars.annotation.ui.annotable.AssociationsTableCell;
import org.mbari.m3.vars.annotation.ui.annotable.FGSTableCell;
import org.mbari.m3.vars.annotation.ui.annotable.FGSValue;
import org.mbari.m3.vars.annotation.util.FormatUtils;
import org.mbari.m3.vars.annotation.util.JFXUtilities;
import org.mbari.vcr4j.time.Timecode;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * @author Brian Schlining
 * @since 2018-04-05T15:00:00
 */
public class AnnotationTableController {

    private TableView<Annotation> tableView;
    private final ResourceBundle i18n;

    public AnnotationTableController(UIToolBox toolBox) {
        this.i18n = toolBox.getI18nBundle();

        // Load the column visibility and width
        loadPreferences();

    }

    private void loadPreferences() {
        // Load the column visibility and width
        Preferences prefs = Preferences.userNodeForPackage(getClass());
        Preferences columnPrefs = prefs.node("table-columns");
        getTableView().getColumns()
                .forEach(tc -> {
                    Preferences p = columnPrefs.node(tc.getId());
                    String s = p.get("visible", "true");
                    boolean isVisible = s.equals("true");
                    String w = p.get("width", "100");
                    double width = Double.parseDouble(w);
                    JFXUtilities.runOnFXThread(() -> {
                        tc.setVisible(isVisible);
                        tc.setPrefWidth(width);
                    });
                });

    }

    public void savePreferences() {
        Preferences prefs = Preferences.userNodeForPackage(getClass());
        Preferences columnPrefs = prefs.node("table-columns");
        getTableView().getColumns()
                .forEach(tc -> {
                    Preferences p = columnPrefs.node(tc.getId());
                    p.put("visible", "false");
                    p.put("width", tc.getWidth() + "");
                });
        getTableView().getVisibleLeafColumns()
                .forEach(tc -> {
                    Preferences p = columnPrefs.node(tc.getId());
                    p.put("visible", "true");
                });
    }

    public TableView<Annotation> getTableView() {
        if (tableView == null) {
            tableView = new TableView<>();
            tableView.setTableMenuButtonVisible(true);

            // --- Add all columns
            TableColumn<Annotation, Instant> timestampCol = new TableColumn<>(i18n.getString("annotable.col.timestamp"));
            timestampCol.setCellValueFactory(new PropertyValueFactory<>("recordedTimestamp"));
            timestampCol.setId("recordedTimestamp");

            TableColumn<Annotation, Timecode> timecodeCol= new TableColumn<>(i18n.getString("annotable.col.timecode"));
            timecodeCol.setCellValueFactory(new PropertyValueFactory<>("timecode"));
            timecodeCol.setId("timecode");

            TableColumn<Annotation, Duration> elapsedTimeCol = new TableColumn<>(i18n.getString("annotable.col.elapsedtime"));
            elapsedTimeCol.setCellValueFactory(new PropertyValueFactory<>("elapsedTime"));
            elapsedTimeCol.setCellFactory( c -> new TableCell<Annotation, Duration>() {
                @Override
                protected void updateItem(Duration item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                    }
                    else {
                        setText(FormatUtils.formatDuration(item));
                    }
                }
            });
            elapsedTimeCol.setId("elapsedTime");

            TableColumn<Annotation, String> obsCol =
                    new TableColumn<>(i18n.getString("annotable.col.concept"));
            obsCol.setCellValueFactory(new PropertyValueFactory<>("concept"));
            obsCol.setId("concept");

            TableColumn<Annotation, List<Association>> assCol =
                    new TableColumn<>(i18n.getString("annotable.col.association"));
            assCol.setCellValueFactory(new PropertyValueFactory<>("associations"));
            assCol.setSortable(false);
            assCol.setCellFactory(c -> {
                AssociationsTableCell cell = new AssociationsTableCell();
                // If association cell is clicked, select the row in the table
                cell.getListView().setOnMouseClicked(event -> {
                    TableRow row = cell.getTableRow();
                    row.getTableView().getSelectionModel().clearAndSelect(row.getIndex(), c);
                });
                return cell;
            });
            assCol.setId("associations");

            TableColumn<Annotation, FGSValue> fgsCol =
                    new TableColumn<>(i18n.getString("annotable.col.framegrab"));
            fgsCol.setCellValueFactory(param ->
                    new SimpleObjectProperty<>(new FGSValue(param.getValue())));
            fgsCol.setSortable(false);
            fgsCol.setCellFactory(c -> new FGSTableCell());
            fgsCol.setId("fgs");


            TableColumn<Annotation, String> obvCol
                    = new TableColumn<>(i18n.getString("annotable.col.observer"));
            obvCol.setCellValueFactory(new PropertyValueFactory<>("observer"));
            obvCol.setId("observer");

            TableColumn<Annotation, Duration> durationCol = new TableColumn<>(i18n.getString("annotable.col.duration"));
            durationCol.setCellValueFactory(new PropertyValueFactory<>("duration"));
            durationCol.setCellFactory(c -> new TableCell<Annotation, Duration>() {
                @Override
                protected void updateItem(Duration item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                    }
                    else {
                        setText(FormatUtils.formatDuration(item));
                    }
                }
            });
            durationCol.setId("duration");

            TableColumn<Annotation, String> actCol
                    = new TableColumn<>(i18n.getString("annotable.col.activity"));
            actCol.setCellValueFactory(new PropertyValueFactory<>("activity"));
            actCol.setId("activity");

            TableColumn<Annotation, String> grpCol
                    = new TableColumn<>(i18n.getString("annotable.col.group"));
            grpCol.setCellValueFactory(new PropertyValueFactory<>("group"));
            grpCol.setId("group");

            TableColumn<Annotation, UUID> vruCol
                    = new TableColumn<>(i18n.getString("annotable.col.videoreference"));
            vruCol.setCellValueFactory(new PropertyValueFactory<>("videoReferenceUuid"));
            vruCol.setId("videoReferenceUuid");


            // TODO get column order from preferences
            tableView.getColumns().addAll(timecodeCol, elapsedTimeCol, timestampCol,
                    obsCol, assCol, fgsCol, obvCol, durationCol, actCol, grpCol, vruCol);


        }
        return tableView;
    }
}
