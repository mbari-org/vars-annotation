/*
 * @(#)FilterComboBox.java   2017.08.17 at 11:06:33 PDT
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

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Brian Schlining
 * @since 2017-08-17T11:05:00
 *
 * @param <T>
 */
public class FilterComboBox<T> extends ComboBox<T> {

    /**
     * The default / initial list that is in the combo when nothing
     * is entered in the editor.
     */
    private Collection<T> initialList = new ArrayList<>();

    /**
     * Check type.  True if this is startsWith, false if it is contains.
     */
    private final boolean startsWithCheck;

    /**
     * Constructs a new FilterComboBox with the given parameters.
     *
     * @param startsWithCheck true if this is a 'startsWith' check false if it is 'contains' check
     */
    public FilterComboBox(final boolean startsWithCheck) {
        super();
        this.startsWithCheck = startsWithCheck;

        super.setEditable(true);

        this.configAutoFilterListener();
    }

    /**
     * Constructs a new FilterComboBox with the given parameters.
     *
     * @param items The initial items
     * @param startsWithCheck true if this is a 'startsWith' check false if it is 'contains' check
     */
    public FilterComboBox(final ObservableList<T> items, final boolean startsWithCheck) {
        super(items);
        this.startsWithCheck = startsWithCheck;
        super.setEditable(true);
        initialList = items;

        this.configAutoFilterListener();
    }

    /**
     * Set up the auto filter on the combo.
     */
    private void configAutoFilterListener() {
        this.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            final T selected = getSelectionModel().getSelectedItem();
            if ((selected ==
            null) || !getConverter().toString(selected).equals(getEditor().getText())) {
                filterItems(newValue);

                if (getItems().size() == 1) {
                    setUserInputToOnlyOption();
                    hide();
                }
                else if (!getItems().isEmpty()) {
                    show();
                }
            }
        });
    }

    /**
     * Method to filter the items and update the combo.
     *
     * @param filter The filter string to use.
     */
    private void filterItems(final String filter) {
        final ObservableList<T> filteredList = FXCollections.observableArrayList();
        for (T item : initialList) {
            if (startsWithCheck &&
                    getConverter().toString(item).toLowerCase().startsWith(filter.toLowerCase())) {
                filteredList.add(item);
            }
            else if (!startsWithCheck &&
                    getConverter().toString(item).toLowerCase().contains(filter.toLowerCase())) {
                filteredList.add(item);
            }
        }

        setItems(filteredList);
    }

    /**
     * Set the initial list of items into this combo box.
     *
     * @param initial The initial list
     */
    public void setInitialItems(final Collection<T> initial) {
        super.getItems()
             .clear();
        super.getItems()
             .addAll(initial);
        this.initialList = initial;
    }

    /**
     * If there is only one item left in the combo then we assume this is correct.
     * Put the item into the editor but select the end of the string that the user
     * hasn't actually entered.
     */
    private void setUserInputToOnlyOption() {
        final String onlyOption = getConverter().toString(getItems().get(0));
        final String currentText = getEditor().getText();
        if (onlyOption.length() > currentText.length()) {
            getEditor().setText(onlyOption);
            Platform.runLater(() -> getEditor().selectAll());
        }
    }
}
