package org.mbari.vars.annotation.ui.util;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

public class JFXUtilitiesAttentionTest {

    private static final String ORIGINAL_STYLE = "-fx-font-size: 14px;";

    @BeforeAll
    public static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        }
        catch (IllegalStateException e) {
            // Toolkit already running
        }
        // These tests open and close windows. Without this, closing the last window shuts
        // down the FX runtime and every later Platform.runLater never runs.
        Platform.setImplicitExit(false);
    }

    private static <T> T fx(Supplier<T> supplier) throws Exception {
        var result = new AtomicReference<T>();
        var error = new AtomicReference<Throwable>();
        var latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                result.set(supplier.get());
            }
            catch (Throwable t) {
                error.set(t);
            }
            finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Timed out waiting for the FX thread");
        if (error.get() != null) {
            throw new AssertionError(error.get());
        }
        return result.get();
    }

    private static Button newButton() {
        var button = new Button("Open", new Text("icon"));
        button.setStyle(ORIGINAL_STYLE);
        return button;
    }

    private static AttentionPulse pulseOf(Button button) {
        return (AttentionPulse) button.getProperties().get(AttentionPulse.PROPERTY_KEY);
    }

    private static long count(Iterable<String> classes, String styleClass) {
        long n = 0;
        for (var c : classes) {
            if (c.equals(styleClass)) {
                n++;
            }
        }
        return n;
    }

    @Test
    public void repeatedCallsAddTheStyleClassesOnce() throws Exception {
        var button = fx(JFXUtilitiesAttentionTest::newButton);
        fx(() -> {
            // e.g. MediaDescriptionEditorPane2Controller calls this on every edit
            JFXUtilities.attractAttention(button);
            JFXUtilities.attractAttention(button);
            JFXUtilities.attractAttention(button);
            assertEquals(1, count(button.getStyleClass(), JFXUtilities.CSS_ATTENTION_BUTTON));
            assertEquals(1, count(button.getGraphic().getStyleClass(), JFXUtilities.CSS_ATTENTION_ICON));

            JFXUtilities.removeAttention(button);
            assertEquals(0, count(button.getStyleClass(), JFXUtilities.CSS_ATTENTION_BUTTON));
            assertEquals(0, count(button.getGraphic().getStyleClass(), JFXUtilities.CSS_ATTENTION_ICON));
            return null;
        });
    }

    @Test
    public void pulseOnlyRunsWhileShowingAndEnabled() throws Exception {
        var button = fx(JFXUtilitiesAttentionTest::newButton);
        var stage = fx(() -> {
            JFXUtilities.attractAttention(button);
            // Not in a window yet, so it must not animate
            assertFalse(pulseOf(button).isRunning());
            assertEquals(ORIGINAL_STYLE, button.getStyle());

            var s = new Stage();
            s.setScene(new Scene(new StackPane(button), 200, 100));
            s.show();
            return s;
        });
        try {
            fx(() -> {
                assertTrue(pulseOf(button).isRunning(), "Should pulse once the window is showing");
                assertTrue(button.getStyle().startsWith(ORIGINAL_STYLE), "Keeps the button's own style");
                assertTrue(button.getStyle().contains("-fx-background-color"));

                button.setDisable(true);
                assertFalse(pulseOf(button).isRunning(), "Should not pulse while disabled");
                assertEquals(ORIGINAL_STYLE, button.getStyle());

                button.setDisable(false);
                assertTrue(pulseOf(button).isRunning());
                return null;
            });

            fx(() -> {
                stage.hide();
                return null;
            });
            fx(() -> {
                assertFalse(pulseOf(button).isRunning(), "Should stop when the window is hidden");
                assertEquals(ORIGINAL_STYLE, button.getStyle());
                return null;
            });
        }
        finally {
            fx(() -> {
                stage.close();
                return null;
            });
        }
    }

    @Test
    public void removeAttentionStopsThePulseAndRestoresTheStyle() throws Exception {
        var button = fx(JFXUtilitiesAttentionTest::newButton);
        var stage = fx(() -> {
            var s = new Stage();
            s.setScene(new Scene(new StackPane(button), 200, 100));
            s.show();
            JFXUtilities.attractAttention(button);
            return s;
        });
        try {
            fx(() -> {
                var pulse = pulseOf(button);
                assertTrue(pulse.isRunning());
                JFXUtilities.removeAttention(button);
                assertFalse(pulse.isRunning());
                assertNull(pulseOf(button));
                assertEquals(ORIGINAL_STYLE, button.getStyle());
                return null;
            });
        }
        finally {
            fx(() -> {
                stage.close();
                return null;
            });
        }
    }

    @Test
    public void worksOnAComboBox() throws Exception {
        // ComboBox isn't Labeled, so there's no graphic to style
        var comboBox = fx(() -> new ComboBox<String>());
        var stage = fx(() -> {
            var s = new Stage();
            s.setScene(new Scene(new StackPane(comboBox), 200, 100));
            s.show();
            JFXUtilities.attractAttention(comboBox);
            return s;
        });
        try {
            fx(() -> {
                assertTrue(comboBox.getStyleClass().contains(JFXUtilities.CSS_ATTENTION_BUTTON));
                var pulse = (AttentionPulse) comboBox.getProperties().get(AttentionPulse.PROPERTY_KEY);
                assertTrue(pulse.isRunning());
                JFXUtilities.removeAttention(comboBox);
                assertFalse(pulse.isRunning());
                assertFalse(comboBox.getStyleClass().contains(JFXUtilities.CSS_ATTENTION_BUTTON));
                return null;
            });
        }
        finally {
            fx(() -> {
                stage.close();
                return null;
            });
        }
    }

    @Test
    public void staticAttentionDoesNotAnimate() throws Exception {
        var button = fx(JFXUtilitiesAttentionTest::newButton);
        fx(() -> {
            JFXUtilities.attractAttention(button, false);
            assertTrue(button.getStyleClass().contains(JFXUtilities.CSS_ATTENTION_BUTTON));
            assertNull(pulseOf(button));
            assertEquals(ORIGINAL_STYLE, button.getStyle());
            return null;
        });
    }

    @Test
    public void inlineStyleUsesDotDecimalsInAnyLocale() throws Exception {
        var defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.GERMANY); // uses ',' as the decimal separator
        var button = fx(JFXUtilitiesAttentionTest::newButton);
        var stage = fx(() -> {
            var s = new Stage();
            s.setScene(new Scene(new StackPane(button), 200, 100));
            s.show();
            JFXUtilities.attractAttention(button);
            return s;
        });
        try {
            var style = fx(button::getStyle);
            assertTrue(style.matches(".*rgba\\(255,183,77,\\d\\.\\d+\\), \\d+\\.\\d.*"),
                    "CSS numbers must use '.', but got: " + style);
        }
        finally {
            Locale.setDefault(defaultLocale);
            fx(() -> {
                JFXUtilities.removeAttention(button);
                stage.close();
                return null;
            });
        }
    }
}
