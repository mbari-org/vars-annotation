package org.mbari.m3.vars.annotation.ui.shared;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTimePicker;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
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
    private final ReadOnlyObjectWrapper<Instant> timestampProperty  = new ReadOnlyObjectWrapper<>();


    public DateTimePickerController() {
        root = new HBox(datePicker, timePicker);

        ObjectBinding<Instant> dateBinding = Bindings.createObjectBinding(
                this::getTimestamp, datePicker.valueProperty());

        ObjectBinding<Instant> timeBinding = Bindings.createObjectBinding(
                this::getTimestamp, timePicker.valueProperty());

        timestampProperty.bind(dateBinding);
        timestampProperty.bind(timeBinding);
        timestampProperty.set(getTimestamp());

    }

    public ReadOnlyObjectProperty<Instant> timestampProperty() {
        return timestampProperty.getReadOnlyProperty();
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

    public void setTimestamp(Instant timestamp) {
        LocalDateTime localDateTime = timestamp.atZone(ZoneId.systemDefault()).toLocalDateTime();
        datePicker.setValue(localDateTime.toLocalDate());
        timePicker.setValue(localDateTime.toLocalTime());
    }



}
