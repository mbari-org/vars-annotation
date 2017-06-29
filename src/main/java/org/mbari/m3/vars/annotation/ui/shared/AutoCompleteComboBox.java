/*
 * @(#)ComboBoxAutoComplete.java   2017.06.28 at 01:56:24 PDT
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
package org.mbari.m3.vars.annotation.ui.shared;

import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;
import org.mbari.m3.vars.annotation.util.StringUtils;

/**
 *
 * @version        $version$, 2017.06.28 at 01:56:24 PDT
 * @author         Brian Schlining <brian@mbari.org>
 *     TODO convert to a ComboBox decorator
 */
public class AutoCompleteComboBox<T> extends ComboBox<T> {

    private static final String EMPTY = "";
    private StringProperty filter = new SimpleStringProperty(EMPTY);
    private AutoCompleteComparator<T> comparator = (typedText, objectToCompare) -> false;
    private ObservableList<T> originalItems = FXCollections.emptyObservableList();


    public AutoCompleteComboBox(AutoCompleteComparator<T> comparator) {
        initialize(comparator);

        itemsProperty().addListener((obs, oldV, newV) -> {
            originalItems = FXCollections.observableArrayList(newV);
            getSelectionModel().selectFirst();
        });

    }

    private ObservableList<T> filterItems() {
        List<T> filteredList = originalItems.stream()
                .filter(el -> comparator.matches(filter.get(), el))
                .collect(Collectors.toList());

        return FXCollections.observableArrayList(filteredList);
    }

    private void handleFilterChanged(String newValue) {
        if (!StringUtils.isBlank(newValue)) {
            show();
            if (StringUtils.isBlank(filter.get())) {
                restoreOriginalItems();
            }
            else {
                showTooltip();
                getItems().setAll(filterItems());
            }
        }
        else {
            getTooltip().hide();
            restoreOriginalItems();
        }
    }

    private void handleOnHiding(Event e) {
        filter.setValue(EMPTY);
        getTooltip().hide();
        restoreOriginalItems();
    }

    private void handleOnKeyPressed(KeyEvent keyEvent) {
        KeyCode code = keyEvent.getCode();
        String filterValue = filter.get();
        if (code.isLetterKey()) {
            filterValue += keyEvent.getText();
        }
        else if ((code == KeyCode.BACK_SPACE) && (filterValue.length() > 0)) {
            filterValue = filterValue.substring(0, filterValue.length() - 1);
        }
        else if (code == KeyCode.ESCAPE) {
            filterValue = EMPTY;
        }
        else if ((code == KeyCode.DOWN) || (code == KeyCode.UP)) {
            show();
        }
        filter.set(filterValue);
        getTooltip().textProperty().set(filterValue);
    }

    /**
     *
     * @param comparator
     */
    public void initialize(AutoCompleteComparator<T> comparator) {
        this.comparator = comparator;
        this.originalItems = FXCollections.observableArrayList(getItems());
        setTooltip(new Tooltip());
//        getTooltip().textProperty()
//                .bind(filter);
        filter.addListener((observable, oldValue, newValue) -> handleFilterChanged(newValue));
        setOnKeyPressed(this::handleOnKeyPressed);
        setOnHidden(this::handleOnHiding);
    }

    private void restoreOriginalItems() {
        T s = getSelectionModel().getSelectedItem();
        getItems().setAll(originalItems);
        getSelectionModel().select(s);
    }

    private void showTooltip() {
        if (!getTooltip().isShowing()) {
            Window stage = getScene().getWindow();
            double posX = stage.getX() + localToScene(getBoundsInLocal()).getMinX() + 4;
            double posY = stage.getY() + localToScene(getBoundsInLocal()).getMinY() - 29;
            getTooltip().show(stage, posX, posY);
        }
    }



    /**
     *
     * @version        $version$, 2017.06.28 at 01:56:24 PDT
     * @author         Brian Schlining <brian@mbari.org>
     */
    public interface AutoCompleteComparator<T> {
        boolean matches(String typedText, T objectToCompare);
    }
}