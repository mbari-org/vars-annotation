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
        setPrefSize(440, 80);
        setMaxSize(440, 80);
        setMinSize(440, 80);
        getChildren().add(clock);
    }
}
