package org.mbari.m3.vars.annotation.ui.annotable;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.mbari.m3.vars.annotation.FormatUtils;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.vcr4j.time.Timecode;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * @author Brian Schlining
 * @since 2017-05-10T10:04:00
 */
public class AnnotationTableController {

    private TableView<Annotation> tableView;
    private final TableColumn<Annotation, Instant> timestampCol;
    private final TableColumn<Annotation, Timecode> timecodeCol;
    private final TableColumn<Annotation, Duration> elapsedTimeCol;

    public AnnotationTableController() {
        timestampCol = new TableColumn<>("Recorded Timestamp");
        timestampCol.setCellValueFactory(new PropertyValueFactory<>("recordedTimestamp"));
        timecodeCol = new TableColumn<>("Timecode");
        timecodeCol.setCellValueFactory(new PropertyValueFactory<>("timecode"));
        elapsedTimeCol = new TableColumn<>("Elapsed Time");
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

        // TODO this should set the index column based on last used in prefs
    }

    public enum TimeIndex {
        TIMESTAMP,
        TIMECODE,
        ELAPSED_TIME
    }


    public TableView<Annotation> getTableView() {
        if (tableView == null) {
            tableView = new TableView<>();
            // TODO add all columns

            TableColumn<Annotation, String> obsCol = new TableColumn<>("Observation");
            obsCol.setCellValueFactory(new PropertyValueFactory<>("concept"));

            TableColumn<Annotation, List<Association>> assCol = new TableColumn<>("Details");
            assCol.setCellValueFactory(new PropertyValueFactory<>("associations"));
            //assCol.setCellFactory(); // TODO add custom renderer to display listview of associations

            //TableColumn<Annotation, >


            tableView.getColumns().addAll(elapsedTimeCol);

        }
        return tableView;
    }

    /**
     * Sets the index column to whatever teh user specifies. This is the first column
     * in the table that shows elapsed time, timecode or recorded timestamp.
     * @param ti
     */
    public void setViewIndex(TimeIndex ti) {
        // TODO show the requested index
        TableColumn<Annotation, ?> idxCol;
        switch (ti) {
            case ELAPSED_TIME:
                idxCol = elapsedTimeCol;
                break;
            case TIMECODE:
                idxCol = timecodeCol;
                break;
            default:
                idxCol = timestampCol;
        }
        getTableView().getColumns().remove(0);
        getTableView().getColumns().add(0, idxCol);
    }
}
