package org.mbari.m3.vars.annotation.ui.annotable;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.util.FormatUtils;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.vcr4j.time.Timecode;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author Brian Schlining
 * @since 2017-05-10T10:04:00
 *
 * TODO all strings need to be put in uiBundle
 */
public class AnnotationTableController {

    private TableView<Annotation> tableView;
    private final ResourceBundle uiBundle;
    private final EventBus eventBus;

    @Inject
    public AnnotationTableController(UIToolBox toolBox) {
        this.uiBundle = toolBox.getI18nBundle();
        this.eventBus = toolBox.getEventBus();

        // TODO this should set the visible columns from prefs
        // TODO this should save the column order from prefs
    }


    public TableView<Annotation> getTableView() {
        if (tableView == null) {
            tableView = new TableView<>();
            tableView.setTableMenuButtonVisible(true);

            // --- Add all columns
            TableColumn<Annotation, Instant> timestampCol = new TableColumn<>(uiBundle.getString("annotable.col.timestamp"));
            timestampCol.setCellValueFactory(new PropertyValueFactory<>("recordedTimestamp"));

            TableColumn<Annotation, Timecode> timecodeCol= new TableColumn<>(uiBundle.getString("annotable.col.timecode"));
            timecodeCol.setCellValueFactory(new PropertyValueFactory<>("timecode"));

            TableColumn<Annotation, Duration> elapsedTimeCol = new TableColumn<>(uiBundle.getString("annotable.col.elapsedtime"));
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

            TableColumn<Annotation, String> obsCol =
                    new TableColumn<>(uiBundle.getString("annotable.col.concept"));
            obsCol.setCellValueFactory(new PropertyValueFactory<>("concept"));

            TableColumn<Annotation, List<Association>> assCol =
                    new TableColumn<>(uiBundle.getString("annotable.col.association"));
            assCol.setCellValueFactory(new PropertyValueFactory<>("associations"));
            assCol.setSortable(false);
            assCol.setCellFactory(c -> new AssociationsTableCell());

            TableColumn<Annotation, FGSValue> fgsCol =
                    new TableColumn<>(uiBundle.getString("annotable.col.framegrab"));
            fgsCol.setCellValueFactory(param ->
                    new SimpleObjectProperty<>(new FGSValue(param.getValue())));
            fgsCol.setSortable(false);
            fgsCol.setCellFactory(c -> new FGSTableCell());

            TableColumn<Annotation, String> obvCol
                    = new TableColumn<>(uiBundle.getString("annotable.col.observer"));
            obvCol.setCellValueFactory(new PropertyValueFactory<>("observer"));

            TableColumn<Annotation, String> actCol
                    = new TableColumn<>(uiBundle.getString("annotable.col.activity"));
            actCol.setCellValueFactory(new PropertyValueFactory<>("activity"));

            TableColumn<Annotation, String> grpCol
                    = new TableColumn<>(uiBundle.getString("annotable.col.group"));
            grpCol.setCellValueFactory(new PropertyValueFactory<>("group"));

            // TODO get column order from preferences
            tableView.getColumns().addAll(timecodeCol, elapsedTimeCol, timestampCol,
                    obsCol, assCol, fgsCol, obvCol, actCol, grpCol);

        }
        return tableView;
    }

}
