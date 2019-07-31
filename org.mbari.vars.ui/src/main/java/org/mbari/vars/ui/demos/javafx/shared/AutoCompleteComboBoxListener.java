package org.mbari.vars.ui.demos.javafx.shared;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2015-07-22T13:44:00
 */
public class AutoCompleteComboBoxListener<T> implements EventHandler<KeyEvent> {

    private final ComboBox<T> comboBox;
    private volatile ObservableList<T> data;
    private boolean moveCaretToPos = false;
    private int caretPos;
    private final Function<T, String> transform;

    public AutoCompleteComboBoxListener(final ComboBox<T> comboBox, final Function<T, String> transform) {
        this.comboBox = comboBox;
        this.transform = transform;

        data = comboBox.getItems();
        comboBox.itemsProperty().addListener((obs, oldv, newv) -> {
            data = newv;
        });

        this.comboBox.setEditable(true);
        this.comboBox.setOnKeyPressed(e -> comboBox.hide());
        this.comboBox.setOnKeyReleased(AutoCompleteComboBoxListener.this);
    }

    public AutoCompleteComboBoxListener(final ComboBox<T> comboBox) {
        this(comboBox, Object::toString);
    }

    @Override
    public void handle(KeyEvent event) {

        TextField editor = comboBox.getEditor();
        String text = editor.getText();

        if (event.getCode() == KeyCode.RIGHT
                || event.getCode() == KeyCode.LEFT
                || event.isControlDown()
                || event.getCode() == KeyCode.HOME
                || event.getCode() == KeyCode.END
                || event.getCode() == KeyCode.TAB) {
            return;
        }
        else if(event.getCode() == KeyCode.UP) {
            caretPos = -1;
            moveCaret(text.length());
            return;
        }
        else if(event.getCode() == KeyCode.DOWN) {
            if(!comboBox.isShowing()) {
                comboBox.show();
            }
            caretPos = -1;
            moveCaret(text.length());
            return;
        }
        else if(event.getCode() == KeyCode.BACK_SPACE) {
            moveCaretToPos = true;
            caretPos = editor.getCaretPosition();
        }
        else if(event.getCode() == KeyCode.DELETE) {
            moveCaretToPos = true;
            caretPos = editor.getCaretPosition();
        }

        String upperCaseText = text.toUpperCase();
        ObservableList<T> list = FXCollections.observableArrayList(data.stream()
                .filter(s -> transform.apply(s).toUpperCase().startsWith(upperCaseText))
                .collect(Collectors.toList()));

        comboBox.setItems(list);

        if (event.getCode() == KeyCode.ENTER
                || event.getCode() == KeyCode.ESCAPE) {
            if (!list.isEmpty()) {
                comboBox.getEditor().setText(transform.apply(list.get(0)));
                comboBox.getSelectionModel().select(0);
                comboBox.hide();
            }
            else {
                comboBox.getEditor().setText(text);
            }

        }
        else {
            comboBox.getEditor().setText(text);
            if(!moveCaretToPos) {
                caretPos = -1;
            }
            moveCaret(text.length());
            if(!list.isEmpty()) {
                comboBox.show();
            }
        }

    }

    private void moveCaret(int textLength) {
        if(caretPos == -1) {
            comboBox.getEditor().positionCaret(textLength);
        } else {
            comboBox.getEditor().positionCaret(caretPos);
        }
        moveCaretToPos = false;
    }

}
