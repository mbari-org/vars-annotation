package org.mbari.vars.ui.demos.javafx.shared;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * @author Brian Schlining
 * @since 2017-05-31T10:25:00
 */
public class BindingDemo {

    public static void main(String[] args) {
        ObjectProperty<LocalDateTime> dp = new SimpleObjectProperty<>(LocalDateTime.now());
        ObjectProperty<Instant> ip = new SimpleObjectProperty<>();

        Binding<Instant> ib = Bindings.createObjectBinding(
                () -> dp.get().toInstant(OffsetDateTime.now().getOffset()),
                dp);
        ip.bind(ib);


        dp.addListener((obs, ov, nv) -> System.out.println(dp.get()));
        ip.addListener((obs, ov, nv) -> System.out.println(ip.get()));

        dp.setValue(LocalDateTime.of(2000, 9, 22, 9, 16, 0));
        dp.setValue(LocalDateTime.of(1968, 12, 25, 8, 0, 0));
        dp.setValue(LocalDateTime.of(2002, 7, 27, 3, 30, 0));


    }
}
