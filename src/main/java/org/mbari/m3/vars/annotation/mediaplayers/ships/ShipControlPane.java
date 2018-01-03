package org.mbari.m3.vars.annotation.mediaplayers.ships;

import eu.hansolo.medusa.Clock;
import eu.hansolo.medusa.ClockBuilder;
import javafx.scene.layout.VBox;

/**
 * @author Brian Schlining
 * @since 2018-01-02T16:49:00
 */
public class ShipControlPane extends VBox {

    public ShipControlPane() {
        Clock clock = ClockBuilder.create()
                .skinType(Clock.ClockSkinType.TEXT)
                .secondsVisible(true)
                .dateVisible(true)
                .build();
        setPrefSize(440, 80);
        setMaxSize(440, 80);
        setMinSize(440, 80);
        getChildren().add(clock);
    }
}
