package org.mbari.m3.vars.annotation.mediaplayers.ships;

import eu.hansolo.medusa.Clock;
import eu.hansolo.medusa.ClockBuilder;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;

import java.time.ZoneId;
import java.util.ResourceBundle;

/**
 * @author Brian Schlining
 * @since 2018-01-02T16:49:00
 */
public class ShipControlPane extends HBox {

    public ShipControlPane(ResourceBundle i18n) {
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
        setAlignment(Pos.CENTER_RIGHT);

        Label label = new Label(ZoneId.systemDefault().getId());
        label.getStyleClass().add("mediaplayer-ship-label");
        label.setPadding(new Insets(5, 5, 5, 5));
        getChildren().addAll(clock, label);
    }
}
