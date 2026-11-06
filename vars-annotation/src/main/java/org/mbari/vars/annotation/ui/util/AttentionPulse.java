package org.mbari.vars.annotation.ui.util;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.Locale;

/**
 * Slowly pulses a control's background color and glow to draw the user's eye to it.
 * Used by {@link JFXUtilities#attractAttention(Control)}.
 *
 * <p>JavaFX CSS can't animate, so a Timeline drives the pulse by writing an inline
 * style (which takes precedence over the {@code .attention-button} stylesheet rule).
 * The pulse only runs while the button is in a showing window, enabled, and not
 * hovered. A running animation makes JavaFX render every frame, so it must not keep
 * running for a button in a closed dialog. While hovered, the inline style is cleared
 * so the stylesheet's {@code :hover} style shows as usual.</p>
 *
 * <p>Must be used on the JavaFX application thread.</p>
 */
final class AttentionPulse {

    /** Key used to store the pulse in the button's properties */
    static final String PROPERTY_KEY = AttentionPulse.class.getName();

    /** Time to go from the base color to the peak color (and the same back again) */
    private static final Duration HALF_PERIOD = Duration.millis(1200);

    /** Matches -attention-color in annotation.css */
    private static final Color BASE_COLOR = Color.web("#FFB74D");
    private static final Color PEAK_COLOR = Color.web("#FFE0B2");

    private final Control button;
    private final String originalStyle;
    private final DoubleProperty phase = new SimpleDoubleProperty(0);
    private final Timeline timeline;
    private final ObservableValue<Boolean> showing;
    private final InvalidationListener updateListener = obs -> update();

    AttentionPulse(Control button) {
        this.button = button;
        this.originalStyle = button.getStyle() == null ? "" : button.getStyle();

        timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(phase, 0, Interpolator.EASE_BOTH)),
                new KeyFrame(HALF_PERIOD, new KeyValue(phase, 1, Interpolator.EASE_BOTH)));
        timeline.setAutoReverse(true);
        timeline.setCycleCount(Animation.INDEFINITE);
        phase.addListener((obs, oldv, newv) -> applyStyle(newv.doubleValue()));

        showing = button.sceneProperty()
                .flatMap(Scene::windowProperty)
                .flatMap(Window::showingProperty)
                .orElse(false);
    }

    void start() {
        showing.addListener(updateListener);
        button.hoverProperty().addListener(updateListener);
        button.disabledProperty().addListener(updateListener);
        update();
    }

    void stop() {
        showing.removeListener(updateListener);
        button.hoverProperty().removeListener(updateListener);
        button.disabledProperty().removeListener(updateListener);
        timeline.stop();
        button.setStyle(originalStyle);
    }

    /** True while the pulse animation is actually running. Visible for testing. */
    boolean isRunning() {
        return timeline.getStatus() == Animation.Status.RUNNING;
    }

    private void update() {
        // Reading the values re-validates the (lazy) bindings so we keep getting notified
        boolean active = showing.getValue() && !button.isHover() && !button.isDisabled();
        if (active) {
            if (!isRunning()) {
                applyStyle(phase.get());
                timeline.play();
            }
        }
        else {
            timeline.pause();
            button.setStyle(originalStyle);
        }
    }

    private void applyStyle(double t) {
        var background = BASE_COLOR.interpolate(PEAK_COLOR, t);
        var glowOpacity = 0.25 + 0.65 * t;
        var glowRadius = 4 + 10 * t;
        // Locale.ROOT so decimals always use '.', which CSS requires
        var animated = String.format(Locale.ROOT,
                "-fx-background-color: %s; -fx-effect: dropshadow(gaussian, rgba(255,183,77,%.2f), %.1f, 0.3, 0, 0);",
                ColorUtil.toHex(background), glowOpacity, glowRadius);
        var separator = originalStyle.isBlank() || originalStyle.strip().endsWith(";") ? " " : "; ";
        button.setStyle(originalStyle.isBlank() ? animated : originalStyle + separator + animated);
    }
}
