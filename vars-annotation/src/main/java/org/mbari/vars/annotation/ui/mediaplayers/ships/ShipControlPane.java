package org.mbari.vars.annotation.ui.mediaplayers.ships;

//import eu.hansolo.medusa.Clock;
//import eu.hansolo.medusa.ClockBuilder;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Brian Schlining
 * @since 2018-01-02T16:49:00
 */
public class ShipControlPane extends StackPane {

    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'")
                    .withZone(ZoneId.systemDefault());

    public ShipControlPane(ResourceBundle i18n) {
//        Clock clock = ClockBuilder.create()
//                .skinType(Clock.ClockSkinType.DIGITAL)
//                .running(true)
//                .secondsVisible(true)
//                .dateVisible(true)
//                .maxHeight(80)
//                .maxWidth(440)
//                .textColor(Color.ORANGERED)
//                .dateColor(Color.DARKGRAY)
//                .build();
//        // Medusa uses AM/PM format for US and 24 hour clock everywhere else
//        // use a non-US locale to get our 24 hour clock
//        // https://github.com/HanSolo/Medusa/blob/master/src/main/java/eu/hansolo/medusa/skins/DigitalClockSkin.java#L130
//        clock.setLocale(Locale.CANADA);
//        setPrefSize(440, 80);
//        setMaxSize(440, 80);
//        setMinSize(440, 80);
//        setAlignment(Pos.CENTER_RIGHT);
//
//        Label label = new Label(ZoneId.systemDefault().getId());
//        label.getStyleClass().add("mediaplayer-ship-label");
//        label.setPadding(new Insets(5, 5, 5, 5));
//        getChildren().addAll(clock, label);
        Label label = new Label();
        label.getStyleClass().add("mediaplayer-ship-label");
        label.setPadding(new Insets(5, 5, 5, 5));
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            String text = formatter.format(Instant.now());
            label.setText(text);
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
        setPrefSize(440, 80);
        setMaxSize(440, 80);
        setMinSize(440, 80);
        setAlignment(Pos.CENTER_RIGHT);
        getChildren().add(label);

    }
}
