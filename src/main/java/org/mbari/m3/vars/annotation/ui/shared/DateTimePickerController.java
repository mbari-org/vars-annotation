package org.mbari.m3.vars.annotation.ui.shared;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTimePicker;
import javafx.scene.layout.HBox;

import java.time.*;

/**
 * @author Brian Schlining
 * @since 2017-05-30T14:45:00
 */
public class DateTimePickerController {

    private final HBox root;

    private final JFXDatePicker datePicker = new JFXDatePicker();
    private final JFXTimePicker timePicker = new JFXTimePicker();


    public DateTimePickerController() {
        root = new HBox(datePicker, timePicker);
    }

    public JFXDatePicker getDatePicker() {
        return datePicker;
    }

    public JFXTimePicker getTimePicker() {
        return timePicker;
    }

    public HBox getRoot() {
        return root;
    }

    public Instant getTimestamp() {
        LocalDate localDate = datePicker.getValue();
        LocalTime localTime = timePicker.getValue();
        return LocalDateTime.of(localDate, localTime)
                .toInstant(OffsetDateTime.now().getOffset());
    }
}
