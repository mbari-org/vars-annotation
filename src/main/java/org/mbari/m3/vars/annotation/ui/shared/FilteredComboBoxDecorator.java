package org.mbari.m3.vars.annotation.ui.shared;

import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.Event;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;
import org.mbari.m3.vars.annotation.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

/**
 * @author Brian Schlining
 * @since 2017-06-28T15:55:00
 */
public class FilteredComboBoxDecorator<T>  {


    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final String EMPTY = "";
    private StringProperty filter = new SimpleStringProperty(EMPTY);
    private AutoCompleteComparator<T> comparator = (typedText, objectToCompare) -> false;
    private volatile FilteredList<T> filteredItems;
    private final ComboBox<T> comboBox;

    private InvalidationListener filterListener = (obs) -> {
        if (filteredItems != null) {
            Predicate<T> p = filter.get().isEmpty() ? null :
                    s -> comparator.matches(filter.get(), s);
            filteredItems.setPredicate(p);
        }
    };

    public FilteredComboBoxDecorator(final ComboBox<T> comboBox,
                                     AutoCompleteComparator<T> comparator) {
        this.comboBox = comboBox;
        filteredItems = new FilteredList<T>(comboBox.getItems());
        filter.addListener(filterListener);

        initialize(comparator);

        comboBox.itemsProperty().addListener((obs, oldV, newV) -> {
            if (!(newV instanceof FilteredList)) {
                filteredItems = new FilteredList<>(newV);
                comboBox.setItems(filteredItems);
            }
            else {
                filteredItems = (FilteredList<T>) newV;
            }
        });
    }


    private void handleFilterChanged(String newValue) {
        if (!StringUtils.isBlank(newValue)) {
            comboBox.show();
            if (StringUtils.isBlank(filter.get())) {
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
        filter.setValue(EMPTY);
        comboBox.getTooltip().hide();
        restoreOriginalItems();
    }

    private void handleOnKeyPressed(KeyEvent keyEvent) {
        KeyCode code = keyEvent.getCode();
        if (!keyEvent.isMetaDown()) {
            String filterValue = filter.get();
            //log.debug("Handling KeyCode = " + code);
            if (code.isLetterKey()) {
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


    public void initialize(AutoCompleteComparator<T> comparator) {
        this.comparator = comparator;
        Tooltip tooltip = new Tooltip();
        tooltip.getStyleClass().add("tooltip-combobox");
        comboBox.setTooltip(tooltip);
        filter.addListener((observable, oldValue, newValue) -> handleFilterChanged(newValue));
        comboBox.setOnKeyPressed(this::handleOnKeyPressed);
        comboBox.setOnHidden(this::handleOnHiding);
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
            (txt, obj) -> obj.toUpperCase().startsWith(txt.toUpperCase());

    public static AutoCompleteComparator<String> CONTAINS_CHARS_IN_ORDER =
            (txt, obj) -> StringUtils.containsOrderedChars(txt.toUpperCase(), obj.toUpperCase());

}

