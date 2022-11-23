/*
 * @(#)AnnotationTableViewFactory.java   2018.11.20 at 02:44:24 PST
 *
 * Copyright 2011 MBARI
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mbari.vars.ui.javafx.shared;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;
import org.mbari.vars.ui.javafx.annotable.AssociationsTableCell;
import org.mbari.vars.ui.javafx.annotable.FGSTableCell;
import org.mbari.vars.ui.javafx.annotable.FGSValue;
import org.mbari.vars.services.util.FormatUtils;
import org.mbari.vcr4j.time.Timecode;

/**
 * Factory for generating the standard table view used in the annotation and
 * bulkeditor windows.
 *
 * @author Brian Schlining
 * @since 2018-11-20T14:37:00
 */
public class AnnotationTableViewFactory {


    private AnnotationTableViewFactory() {}

    /**
     * @return A new instance of a table view for displaying Annotations
     */
    public static TableView<Annotation> newTableView(ResourceBundle i18n) {
            TableView<Annotation> tableView = new TableView<>();
            tableView.setTableMenuButtonVisible(true);

            // --- Add all columns
            TableColumn<Annotation, Instant> timestampCol = new TableColumn<>(
                i18n.getString("annotable.col.timestamp"));
            timestampCol.setCellValueFactory(new PropertyValueFactory<>("recordedTimestamp"));
            timestampCol.setId("recordedTimestamp");

            TableColumn<Annotation, Timecode> timecodeCol = new TableColumn<>(
                i18n.getString("annotable.col.timecode"));
            timecodeCol.setCellValueFactory(new PropertyValueFactory<>("timecode"));
            timecodeCol.setId("timecode");

            TableColumn<Annotation, Duration> elapsedTimeCol = new TableColumn<>(
                i18n.getString("annotable.col.elapsedtime"));
            elapsedTimeCol.setCellValueFactory(new PropertyValueFactory<>("elapsedTime"));
            elapsedTimeCol.setCellFactory(c -> new TableCell<Annotation, Duration>() {

                        @Override
                        protected void updateItem(Duration item, boolean empty) {
                            super.updateItem(item, empty);
                            if ((item == null) || empty) {
                                setText(null);
                            }
                            else {
                                setText(FormatUtils.formatDuration(item));
                            }
                        }

                    });
            elapsedTimeCol.setId("elapsedTime");

            TableColumn<Annotation, String> obsCol = new TableColumn<>(
                i18n.getString("annotable.col.concept"));
            obsCol.setCellValueFactory(new PropertyValueFactory<>("concept"));
            obsCol.setId("concept");

            TableColumn<Annotation, List<Association>> assCol = new TableColumn<>(
                i18n.getString("annotable.col.association"));
            assCol.setCellValueFactory(new PropertyValueFactory<>("associations"));
            assCol.setSortable(false);
            assCol.setCellFactory(
                c -> {
                    AssociationsTableCell cell = new AssociationsTableCell();

                    // If association cell is clicked, select the row in the table
                    cell.getListView().setOnMouseClicked(event -> {
                            TableRow row = cell.getTableRow();
                            row.getTableView()
                                    .getSelectionModel()
                                    .clearAndSelect(row.getIndex(), c);
                        });

                    return cell;
                });
            assCol.setId("associations");

            TableColumn<Annotation, Integer> assCountCol = new TableColumn<>(
                    i18n.getString("annotable.col.associationcount"));
            assCountCol.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getAssociations().size()));
            assCountCol.setSortable(true);
            assCountCol.setId("associations-count");

            TableColumn<Annotation, FGSValue> fgsCol = new TableColumn<>(
                i18n.getString("annotable.col.framegrab"));
            fgsCol.setCellValueFactory(
                param -> new SimpleObjectProperty<>(new FGSValue(param.getValue())));
            fgsCol.setSortable(false);
            fgsCol.setCellFactory(c -> new FGSTableCell());
            fgsCol.setId("fgs");

            TableColumn<Annotation, String> obvCol = new TableColumn<>(
                i18n.getString("annotable.col.observer"));
            obvCol.setCellValueFactory(new PropertyValueFactory<>("observer"));
            obvCol.setId("observer");

            TableColumn<Annotation, Duration> durationCol = new TableColumn<>(
                i18n.getString("annotable.col.duration"));
            durationCol.setCellValueFactory(new PropertyValueFactory<>("duration"));
            durationCol.setCellFactory(c -> new TableCell<Annotation, Duration>() {

                        @Override
                        protected void updateItem(Duration item, boolean empty) {
                            super.updateItem(item, empty);
                            if ((item == null) || empty) {
                                setText(null);
                            }
                            else {
                                setText(FormatUtils.formatDuration(item));
                            }
                        }

                    });
            durationCol.setId("duration");

            TableColumn<Annotation, String> actCol = new TableColumn<>(
                i18n.getString("annotable.col.activity"));
            actCol.setCellValueFactory(new PropertyValueFactory<>("activity"));
            actCol.setId("activity");

            TableColumn<Annotation, String> grpCol = new TableColumn<>(
                i18n.getString("annotable.col.group"));
            grpCol.setCellValueFactory(new PropertyValueFactory<>("group"));
            grpCol.setId("group");



            // TODO get column order from preferences
            tableView.getColumns()
                    .addAll(timecodeCol,
                            elapsedTimeCol,
                            timestampCol,
                            obsCol,
                            assCol,
                            assCountCol,
                            fgsCol,
                            obvCol,
                            durationCol,
                            actCol,
                            grpCol);

            TableView.TableViewSelectionModel<Annotation> selectionModel = tableView.getSelectionModel();
            selectionModel.setSelectionMode(SelectionMode.MULTIPLE);


        return tableView;
    }
}
