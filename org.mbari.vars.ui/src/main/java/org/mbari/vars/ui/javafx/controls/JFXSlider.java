package org.mbari.vars.ui.javafx.controls;


import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.*;
import javafx.scene.control.Skin;
import javafx.scene.control.Slider;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JFXSlider is the material design implementation of a slider.
 *
 * @author Bashir Elias & Shadi Shaheen
 * @version 1.0
 * @since 2016-03-09
 */
public class JFXSlider extends Slider {

    /**
     * {@inheritDoc}
     */
    public JFXSlider() {
        super(0, 100, 50);
        initialize();
    }

    /**
     * {@inheritDoc}
     */
    public JFXSlider(double min, double max, double value) {
        super(min, max, value);
        initialize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new JFXSliderSkin(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserAgentStylesheet() {
        return getClass().getResource("/css/controls/jfx-slider.css").toExternalForm();
    }

    private void initialize() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    public enum IndicatorPosition {
        LEFT, RIGHT
    }

    /***************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/

    /**
     * String binding factory for the slider value.
     * Sets a custom string for the value text (by default, it shows the value rounded to the nearest whole number).
     * <p>
     * <p>For example, to have the value displayed as a percentage (assuming the slider has a range of (0, 100)):
     * <pre><code>
     * JFXSlider mySlider = ...
     * mySlider.setValueFactory(slider ->
     * 		Bindings.createStringBinding(
     * 			() -> ((int) slider.getValue()) + "%",
     * 			slider.valueProperty()
     * 		)
     * );
     * </code></pre>
     * <p>
     * NOTE: might be replaced later with a call back to create the animated thumb node
     *
     * @param callback a callback to create the string value binding
     */
    private ObjectProperty<Callback<JFXSlider, StringBinding>> valueFactory;

    public final ObjectProperty<Callback<JFXSlider, StringBinding>> valueFactoryProperty() {
        if (valueFactory == null) {
            valueFactory = new SimpleObjectProperty<>(this, "valueFactory");
        }
        return valueFactory;
    }

    /**
     * @return the current slider value factory
     */
    public final Callback<JFXSlider, StringBinding> getValueFactory() {
        return valueFactory == null ? null : valueFactory.get();
    }

    /**
     * sets custom string binding for the slider text value
     *
     * @param valueFactory a callback to create the string value binding
     */
    public final void setValueFactory(final Callback<JFXSlider, StringBinding> valueFactory) {
        this.valueFactoryProperty().set(valueFactory);
    }

    /***************************************************************************
     *                                                                         *
     * Stylesheet Handling                                                     *
     *                                                                         *
     **************************************************************************/

    /**
     * Initialize the style class to 'jfx-slider'.
     * <p>
     * This is the selector class from which CSS can be used to style
     * this control.
     */
    private static final String DEFAULT_STYLE_CLASS = "jfx-slider";

    /**
     * indicates the position of the slider indicator, can be
     * either LEFT or RIGHT
     */
    private StyleableObjectProperty<IndicatorPosition> indicatorPosition = new SimpleStyleableObjectProperty<>(
            StyleableProperties.INDICATOR_POSITION,
            JFXSlider.this,
            "indicatorPosition",
            IndicatorPosition.LEFT);

    public IndicatorPosition getIndicatorPosition() {
        return indicatorPosition == null ? IndicatorPosition.LEFT : indicatorPosition.get();
    }

    public StyleableObjectProperty<IndicatorPosition> indicatorPositionProperty() {
        return this.indicatorPosition;
    }

    public void setIndicatorPosition(IndicatorPosition pos) {
        this.indicatorPosition.set(pos);
    }

    private static class StyleableProperties {
        private static final CssMetaData<JFXSlider, IndicatorPosition> INDICATOR_POSITION = new CssMetaData<JFXSlider, IndicatorPosition>(
                "-jfx-indicator-position",
                IndicatorPositionConverter.getInstance(),
                IndicatorPosition.LEFT) {
            @Override
            public boolean isSettable(JFXSlider control) {
                return control.indicatorPosition == null || !control.indicatorPosition.isBound();
            }

            @Override
            public StyleableProperty<IndicatorPosition> getStyleableProperty(JFXSlider control) {
                return control.indicatorPositionProperty();
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> CHILD_STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(
                    Slider.getClassCssMetaData());
            Collections.addAll(styleables, INDICATOR_POSITION);
            CHILD_STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.CHILD_STYLEABLES;
    }
}
