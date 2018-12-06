package org.mbari.m3.vars.annotation.util;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

/**
 * @author Brian Schlining
 * @since 2018-03-23T13:26:00
 */
public class JFXUtilities {

    private static final String CSS_ATTENTION_BUTTON = "attention-button";
    private static final String CSS_ATTENTION_ICON = "attention-icon";

    private static final String WIDTH_KEY = "stage-width";
    private static final String HEIGHT_KEY = "stage-height";

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

    /**
     *
     * @param stage The Stage
     * @param clazz Class used to lookup preferences.
     */
    public static void loadStageSize(Stage stage, Class clazz) {
        Preferences prefs = Preferences.userNodeForPackage(clazz);
        double width = prefs.getDouble(WIDTH_KEY, 1000D);
        double height = prefs.getDouble(HEIGHT_KEY, 800D);

        // ON rare occasions the user sets one of these to 0 and are never
        // able to see the annotation window again. Make sure this doesn't happen.
        if (width < 200) {
            width = 200;
        }
        if (height < 200) {
            height = 200;
        }
        stage.setWidth(width);
        stage.setHeight(height);
    }

    public static void saveStageSize(Stage stage, Class clazz) {
        Preferences prefs = Preferences.userNodeForPackage(clazz);
        prefs.putDouble(WIDTH_KEY, stage.getWidth());
        prefs.putDouble(HEIGHT_KEY, stage.getHeight());
    }
}
