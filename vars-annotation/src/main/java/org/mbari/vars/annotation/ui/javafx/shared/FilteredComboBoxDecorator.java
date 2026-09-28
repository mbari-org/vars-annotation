package org.mbari.vars.annotation.ui.javafx.shared;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.Event;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tooltip;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;
import org.mbari.vars.annotation.etc.jdk.Loggers;
import org.mbari.vars.annotation.etc.jdk.Strings;


import java.util.function.Predicate;

/**
 * @author Brian Schlining
 * @since 2017-06-28T15:55:00
 */
public class FilteredComboBoxDecorator<T>  {


    private final Loggers log = new Loggers(getClass());
    private static final String EMPTY = "";
    private StringProperty filter = new SimpleStringProperty(EMPTY);
    private AutoCompleteComparator<T> comparator;
    private final ObservableList<T> backingItems = FXCollections.observableArrayList();
    private volatile FilteredList<T> filteredItems;
    private final ComboBox<T> comboBox;

    public FilteredComboBoxDecorator(final ComboBox<T> comboBox,
                                     AutoCompleteComparator<T> comparator) {
        this.comboBox = comboBox;
        this.comparator = comparator;

        // The combobox always shows this one FilteredList. If someone later calls
        // comboBox.setItems(...) we copy the new items into `backingItems` rather than
        // wrapping the new list in a new FilteredList. (Swapping the wrapper from inside the
        // items listener leaves the combobox showing the unfiltered list.)
        backingItems.setAll(comboBox.getItems());
        filteredItems = new FilteredList<>(backingItems);
        comboBox.setItems(filteredItems);

        Tooltip tooltip = new Tooltip();
        tooltip.getStyleClass().add("tooltip-combobox");
        comboBox.setTooltip(tooltip);
        filter.addListener((observable, oldValue, newValue) -> handleFilterChanged(newValue));
        comboBox.setOnKeyPressed(this::handleOnKeyPressed);
        comboBox.setOnHidden(this::handleOnHiding);

        // Use an InvalidationListener, NOT a ChangeListener. A ChangeListener is only notified when
        // !oldValue.equals(newValue), and ObservableList.equals compares contents. So setting a new
        // list with the same contents as the current one (e.g. on a refresh) would go unnoticed and
        // the combobox would be left showing the unfiltered list.
        comboBox.itemsProperty().addListener(obs -> {
            var newItems = comboBox.getItems(); // also revalidates the property so we get the next event
            if (newItems != filteredItems) {
                backingItems.setAll(newItems);
                comboBox.setItems(filteredItems);
            }
        });

        // HACK workaround for bug that consumes spaces in combobox
        // this may be different in JDK 9+
        // https://stackoverflow.com/questions/50013972/how-to-prevent-closing-of-autocompletecombobox-popupmenu-on-space-key-press-in-j
        ComboBoxListViewSkin<T> skin = new ComboBoxListViewSkin<>(comboBox);
        skin.getPopupContent().addEventFilter(KeyEvent.ANY, e -> {
            if (e.getCode() == KeyCode.SPACE) {
                e.consume();
            }
        });
        comboBox.setSkin(skin);

        installValueBasedButtonCell();
    }

    /**
     * The combobox's default button cell (the one that shows the selected item) renders whatever row
     * it was last told to point at. Un-filtering the list moves the selected item to a different row,
     * and if the value itself doesn't change the skin never re-points the cell, so it keeps showing
     * the item that now sits at the old row (e.g. row 0) while the value is right. So we use a button
     * cell that displays the combobox's value instead of an item looked up by index.
     */
    private void installValueBasedButtonCell() {
        if (comboBox.getButtonCell() != null) {
            return; // The owner has their own cell. Leave it alone.
        }
        var cell = new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                showValue(this);
            }
        };
        comboBox.valueProperty().addListener(obs -> showValue(cell));
        comboBox.setButtonCell(cell);
        showValue(cell);
    }

    private void showValue(ListCell<T> cell) {
        T value = comboBox.getValue();
        if (value == null) {
            cell.setText(comboBox.getPromptText());
        }
        else {
            var converter = comboBox.getConverter();
            cell.setText(converter == null ? String.valueOf(value) : converter.toString(value));
        }
    }


    /**
     * Logs (at DEBUG) the state of the combobox's selection. Used to diagnose cases where the displayed
     * item doesn't match the value; look for "FilteredComboBoxDecorator" in the log.
     */
    private void logState(String where) {
        log.atDebug().log(() -> {
            var sm = comboBox.getSelectionModel();
            var items = comboBox.getItems();
            int idx = sm.getSelectedIndex();
            var atIdx = idx > -1 && idx < items.size() ? String.valueOf(items.get(idx)) : "<n/a>";
            return "FilteredComboBoxDecorator[" + where + "] filter='" + filter.get() + "' value=" + comboBox.getValue()
                    + " selectedItem=" + sm.getSelectedItem() + " selectedIndex=" + idx
                    + " itemAtSelectedIndex=" + atIdx + " indexOfValue=" + items.indexOf(comboBox.getValue())
                    + " items=" + items.size() + " showing=" + comboBox.isShowing();
        });
    }

    private void handleFilterChanged(String newValue) {
        if (filteredItems != null) {
            Predicate<T> p = filter.get().isEmpty() ? null :
                    s -> comparator.matches(filter.get(), s);
            filteredItems.setPredicate(p);
        }

        if (!Strings.isBlank(newValue)) {
            comboBox.show();
            if (Strings.isBlank(filter.get())) {
                restoreOriginalItems();
            }
            else {
                showTooltip();
                SingleSelectionModel<T> selectionModel = comboBox.getSelectionModel();
                if (filteredItems.isEmpty()) {
                    selectionModel.clearSelection();
                }
                else {
                    selectionModel.select(0);
                }
            }
        }
        else {
            comboBox.getTooltip().hide();
            restoreOriginalItems();
        }
    }

    private void handleOnHiding(Event e) {
        // Don't un-filter the list synchronously. When the popup hides because the user clicked an
        // item, the click may not be fully processed yet. If the list changes underneath it, the
        // clicked row (say, row 0 of the filtered list) is resolved against the un-filtered list and
        // the wrong item (row 0 of the full list) ends up selected. So let the click finish first.
        comboBox.getTooltip().hide();
        logState("hidden");
        Platform.runLater(this::resetFilter);
    }

    private void resetFilter() {
        T value = comboBox.getValue();
        logState("resetFilter:start");
        filter.setValue(EMPTY);
        logState("resetFilter:filterCleared");
        if (value != null) {
            comboBox.getSelectionModel().select(value);
        }
        comboBox.getTooltip().hide();
        restoreOriginalItems();
        logState("resetFilter:end");
        // The display can lag behind the selection, so log again after the UI has caught up
        Platform.runLater(() -> logState("resetFilter:afterPulse"));
    }

    private void handleOnKeyPressed(KeyEvent keyEvent) {
        KeyCode code = keyEvent.getCode();
        if (!keyEvent.isMetaDown()) {
            String filterValue = filter.get();
            log.atDebug().log("Handling KeyCode = " + code);
            if (code.isLetterKey() || code.isDigitKey() || code == KeyCode.MINUS) {
                filterValue += keyEvent.getText();
            } else if ((code == KeyCode.BACK_SPACE) && (filterValue.length() > 0)) {
                filterValue = filterValue.substring(0, filterValue.length() - 1);
            } else if (code == KeyCode.ESCAPE) {
                filterValue = EMPTY;
            } else if ((code == KeyCode.DOWN) || (code == KeyCode.UP)) {
                comboBox.show();
            }
            filter.set(filterValue);
            comboBox.getTooltip().textProperty().set(filterValue);
        }
    }

    /**
     *
     * @param comparator Can not be null
     */
    public void setComparator(AutoCompleteComparator<T> comparator) {
        this.comparator = comparator;
        handleFilterChanged(filter.get());
    }

    private void restoreOriginalItems() {
        T s = comboBox.getSelectionModel().getSelectedItem();
        comboBox.getSelectionModel().select(s);
    }

    private void showTooltip() {
        if (!comboBox.getTooltip().isShowing()) {
            Window stage = comboBox.getScene().getWindow();
            double posX = stage.getX() +
                    comboBox.localToScene(comboBox.getBoundsInLocal()).getMinX() + 4;
            double posY = stage.getY() +
                    comboBox.localToScene(comboBox.getBoundsInLocal()).getMinY() - 29;
            comboBox.getTooltip().show(stage, posX, posY);
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

    public static AutoCompleteComparator<String> STARTSWITH =
            (txt, obj) -> obj.toUpperCase()
                    .startsWith(txt.toUpperCase());

    public static AutoCompleteComparator<String> STARTSWITH_IGNORE_SPACES =
            (txt, obj) -> obj.replace(" ", "")
                    .toUpperCase()
                    .startsWith(txt.replace(" ", "").toUpperCase());

    public static AutoCompleteComparator<String> CONTAINS_CHARS_IN_ORDER =
            (txt, obj) -> Strings.containsOrderedChars(txt.toUpperCase(), obj.toUpperCase());

    public static AutoCompleteComparator<String> STARTSWITH_FIRST_THEN_CONTAINS_CHARS_IN_ORDER =
            (txt, obj) -> obj.substring(0, 1).equalsIgnoreCase(txt.substring(0, 1)) &&
                    Strings.containsOrderedChars(txt.toUpperCase(), obj.toUpperCase());


}

