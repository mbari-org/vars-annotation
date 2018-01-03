package org.mbari.m3.vars.annotation.mediaplayers.ships;

import eu.hansolo.medusa.Clock;
import eu.hansolo.medusa.ClockBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * @author Brian Schlining
 * @since 2018-01-02T16:49:00
 */
public class ShipControlPane extends VBox {

    public ShipControlPane() {
        Clock clock = ClockBuilder.create()
                .skinType(Clock.ClockSkinType.DIGITAL)
                .running(true)
                .secondsVisible(true)
                .dateVisible(true)
                .maxHeight(80)
                .maxWidth(440)
                .textColor(Color.ORANGERED)
                .dateColor(Color.DARKGRAY)
                .build();

//        Clock clock = ClockBuilder.create()
//                .skinType(Clock.ClockSkinType.INDUSTRIAL)
//                .prefSize(400, 400)
//                .locale(Locale.GERMANY)
//                .shadowsEnabled(true)
//                .running(true)
//                .backgroundPaint(Color.web("#1f1e23"))
//                .hourColor(Color.web("#dad9db"))
//                .minuteColor(Color.web("#dad9db"))
//                .secondColor(Color.web("#d1222b"))
//                .hourTickMarkColor(Color.web("#9f9fa1"))
//                .minuteTickMarkColor(Color.web("#9f9fa1"))
//                .build();
        setPrefSize(440, 80);
        setMaxSize(440, 80);
        setMinSize(440, 80);
        getChildren().add(clock);
    }
}
