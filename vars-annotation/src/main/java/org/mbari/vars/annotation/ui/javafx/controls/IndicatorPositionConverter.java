package org.mbari.vars.ui.javafx.controls;


import javafx.css.ParsedValue;
import javafx.css.StyleConverter;
import javafx.scene.text.Font;

/**
 * Converts the CSS for -fx-indicator-position items into IndicatorPosition.
 * it's used in JFXSlider.
 *
 * @author Shadi Shaheen
 * @version 1.0
 * @since 2016-03-09
 */
public class IndicatorPositionConverter extends StyleConverter<String, JFXSlider.IndicatorPosition> {
    // lazy, thread-safe instatiation
    private static class Holder {
        static final IndicatorPositionConverter INSTANCE = new IndicatorPositionConverter();
    }

    public static StyleConverter<String, JFXSlider.IndicatorPosition> getInstance() {
        return Holder.INSTANCE;
    }

    private IndicatorPositionConverter() {
    }

    @Override
    public JFXSlider.IndicatorPosition convert(ParsedValue<String, JFXSlider.IndicatorPosition> value, Font not_used) {
        String string = value.getValue().toUpperCase();
        try {
            return JFXSlider.IndicatorPosition.valueOf(string);
        } catch (IllegalArgumentException | NullPointerException exception) {
            return JFXSlider.IndicatorPosition.LEFT;
        }
    }

    @Override
    public String toString() {
        return "IndicatorPositionConverter";
    }

}
