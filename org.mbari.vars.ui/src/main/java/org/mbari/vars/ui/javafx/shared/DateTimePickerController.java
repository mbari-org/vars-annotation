package org.mbari.vars.ui.javafx.shared;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.HBox;

import java.time.*;

/**
 * @author Brian Schlining
 * @since 2017-05-30T14:45:00
 */
public class DateTimePickerController {

    private final HBox root;

    private final DatePicker datePicker = new DatePicker(LocalDate.now());
//    private final TimePicker timePicker = new TimePicker(LocalTime.now());
    private final ReadOnlyObjectWrapper<Instant> timestampProperty  = new ReadOnlyObjectWrapper<>();


    public DateTimePickerController() {

       //root = new HBox(datePicker, timePicker);
        root = new HBox(datePicker);

        ObjectBinding<Instant> dateBinding = Bindings.createObjectBinding(
                this::getTimestamp, datePicker.valueProperty());

//        ObjectBinding<Instant> timeBinding = Bindings.createObjectBinding(
//                this::getTimestamp, timePicker.valueProperty());

        timestampProperty.bind(dateBinding);
//        timestampProperty.bind(timeBinding);

    }

    public ReadOnlyObjectProperty<Instant> timestampProperty() {
        return timestampProperty.getReadOnlyProperty();
    }

    public DatePicker getDatePicker() {
        return datePicker;
    }

//    public JFXTimePicker getTimePicker() {
//        return timePicker;
//    }

    public HBox getRoot() {
        return root;
    }

    public Instant getTimestamp() {
        LocalDate localDate = datePicker.getValue();
//        LocalTime localTime = timePicker.getValue();
        return LocalDateTime.of(localDate, LocalTime.now())
                .toInstant(OffsetDateTime.now().getOffset());
    }

    public void setTimestamp(Instant timestamp) {
        LocalDateTime localDateTime = timestamp.atZone(ZoneId.systemDefault()).toLocalDateTime();
        datePicker.setValue(localDateTime.toLocalDate());
//        timePicker.setValue(localDateTime.toLocalTime());
    }



}
