package org.mbari.m3.vars.annotation.ui.shared;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.function.Function;

/**
 * @author Brian Schlining
 * @since 2017-06-28T15:55:00
 */
public class FilteredComboBoxDecorator<T> implements EventHandler<KeyEvent> {

    private final Tooltip tooltip = new Tooltip();
    private final StringProperty typedValue = new SimpleStringProperty("");
    private final ObjectProperty<ObservableList<T>> originalItems =
            new SimpleObjectProperty<>(FXCollections.emptyObservableList());
    private final ComboBox<T> comboBox;

    public FilteredComboBoxDecorator(final ComboBox<T> comboBox, final Function<T, String> transform) {
        this.comboBox = comboBox;
        originalItems.bind(comboBox.itemsProperty());
        comboBox.setTooltip(tooltip);
        comboBox.setEditable(false);
        comboBox.setOnKeyPressed(e -> comboBox.hide());
        comboBox.setOnKeyReleased(FilteredComboBoxDecorator.this);
        tooltip.textProperty().bind(typedValue);
        typedValue.addListener((obs, oldV, newV) -> {

        });
    }

    @Override
    public void handle(KeyEvent event) {
        KeyCode code = event.getCode();
        String text = typedValue.get();
        if (code.isLetterKey()) {
            text += event.getText();
        }
        else if ((code == KeyCode.BACK_SPACE) && (text.length() > 0)) {
            text = text.substring(0, text.length() - 1);
        }
        else if (code == KeyCode.ESCAPE) {
            text = "";
        }
        else if ((code == KeyCode.DOWN) || (code == KeyCode.UP)) {
            comboBox.show();
        }
        typedValue.set(text);
    }

    private void handleTyping(String text) {

    }

    private static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
