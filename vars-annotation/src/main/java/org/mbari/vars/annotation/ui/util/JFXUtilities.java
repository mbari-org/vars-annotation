package org.mbari.vars.annotation.ui.util;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Labeled;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;

import java.util.function.Supplier;
import java.util.prefs.Preferences;

/**
 * @author Brian Schlining
 * @since 2018-03-23T13:26:00
 */
public class JFXUtilities {

    public static final String CSS_ATTENTION_BUTTON = "attention-button";
    public static final String CSS_ATTENTION_ICON = "attention-icon";

    private static final String WIDTH_KEY = "stage-width";
    private static final String HEIGHT_KEY = "stage-height";

    public static void runOnFXThread(Runnable r) {
        Platform.runLater(r);
//        if (Platform.isFxApplicationThread()) {
//            r.run();
//        }
//        else {
//            Platform.runLater(r);
//        }
    }

    /**
     * Focus a node every time the dialog is shown. The request has to wait until the dialog is
     * visible; otherwise a reused dialog's scene restores its previous focus owner (e.g. the OK button).
     *
     * @param dialog The dialog
     * @param target Supplies the node to focus. Called each time the dialog is shown and may return null.
     */
    public static void focusOnShown(Dialog<?> dialog, Supplier<? extends Node> target) {
        dialog.setOnShown(e -> Platform.runLater(() -> {
            Node node = target.get();
            if (node != null) {
                node.requestFocus();
            }
        }));
    }

    /**
     * Highlights a control (e.g. a Button or ComboBox) to indicate an action the user should
     * take. The highlight slowly pulses while the control is visible, enabled, and not under
     * the mouse. Safe to call repeatedly. Must be called on the JavaFX application thread.
     *
     * @param button The control
     */
    public static void attractAttention(Control button) {
        attractAttention(button, true);
    }

    /**
     * Highlights a control. Safe to call repeatedly. Must be called on the JavaFX application thread.
     *
     * @param button The control
     * @param animate true to slowly pulse the highlight (for an action the user should take),
     *                false for a static highlight (e.g. to mark the last button pressed)
     */
    public static void attractAttention(Control button, boolean animate) {
        // Only add the classes once. Adding duplicates meant removeAttention (which removed
        // a single copy) could leave a button highlighted.
        addStyleClassOnce(button, CSS_ATTENTION_BUTTON);
        Node graphic = graphicOf(button);
        if (graphic != null) {
            addStyleClassOnce(graphic, CSS_ATTENTION_ICON);
        }

        var properties = button.getProperties();
        if (animate && !properties.containsKey(AttentionPulse.PROPERTY_KEY)) {
            var pulse = new AttentionPulse(button);
            properties.put(AttentionPulse.PROPERTY_KEY, pulse);
            pulse.start();
        }
        else if (!animate) {
            stopPulse(button);
        }
    }

    public static void removeAttention(Control button) {
        stopPulse(button);
        button.getStyleClass().removeAll(CSS_ATTENTION_BUTTON);
        Node graphic = graphicOf(button);
        if (graphic != null) {
            graphic.getStyleClass().removeAll(CSS_ATTENTION_ICON);
        }
    }

    private static Node graphicOf(Control control) {
        return control instanceof Labeled labeled ? labeled.getGraphic() : null;
    }

    private static void stopPulse(Control button) {
        if (button.getProperties().remove(AttentionPulse.PROPERTY_KEY) instanceof AttentionPulse pulse) {
            pulse.stop();
        }
    }

    private static void addStyleClassOnce(Node node, String styleClass) {
        if (!node.getStyleClass().contains(styleClass)) {
            node.getStyleClass().add(styleClass);
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
