package org.mbari.m3.vars.annotation.util;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;

/**
 * @author Brian Schlining
 * @since 2018-03-23T13:26:00
 */
public class JFXUtilities {

    private static final String CSS_ATTENTION_BUTTON = "attention-button";
    private static final String CSS_ATTENTION_ICON = "attention-icon";

    public static void runOnFXThread(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        }
        else {
            Platform.runLater(r);
        }
    }

    public static void attractAttention(Button button) {
        button.getStyleClass().add(CSS_ATTENTION_BUTTON);
        Node graphic = button.getGraphic();
        if (graphic != null) {
            graphic.getStyleClass().add(CSS_ATTENTION_ICON);
        }
    }

    public static void removeAttention(Button button) {
        button.getStyleClass().remove(CSS_ATTENTION_BUTTON);
        Node graphic = button.getGraphic();
        if (graphic != null) {
            graphic.getStyleClass().remove(CSS_ATTENTION_ICON);
        }
    }
}
