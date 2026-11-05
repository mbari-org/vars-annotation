package org.mbari.vars.annotation.ui.javafx.controls;


import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Slider;
import javafx.scene.control.skin.SliderSkin;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * <h1>Material Design Slider Skin</h1>
 * <p>
 * rework of JFXSliderSkin by extending Java SliderSkin
 * this solves padding and resizing issues
 * <p>
 * The tear-drop value indicator is rendered in a {@link Popup} rather than as a
 * child of the slider so that it is always drawn above every other control in
 * the window (and is never clipped by the slider's ancestors).
 *
 * @author Shadi Shaheen
 * @version 1.0
 * @since 2016-03-09
 */
public class JFXSliderSkin extends SliderSkin {


    private final Pane mouseHandlerPane = new Pane();
    private static final PseudoClass MIN_VALUE = PseudoClass.getPseudoClass("min");
    private static final PseudoClass MAX_VALUE = PseudoClass.getPseudoClass("max");

    private Text sliderValue;
    private StackPane coloredTrack;
    private StackPane thumb;
    private StackPane track;
    private StackPane animatedThumb;
    private NumberAxis tickLine;

    private Timeline timeline;

    /** Window that hosts the animated indicator so it floats above all other nodes */
    private final Popup indicatorPopup = new Popup();
    /** Root of the popup, centered on the thumb. Mirrors the slider's style so CSS still applies */
    private final Pane indicatorRoot = new Pane();
    /** 0 = indicator collapsed onto the thumb, 1 = fully extended. Driven by the timeline */
    private final DoubleProperty indicatorProgress = new SimpleDoubleProperty(0);
    /** Guards against re-entry: resizing the bubble fires its layoutBounds listener */
    private boolean layingOutIndicator;

    private double indicatorRotation;
    private double horizontalRotation;


    public JFXSliderSkin(JFXSlider slider) {
        super(slider);

        track = (StackPane) getSkinnable().lookup(".track");
        thumb = (StackPane) getSkinnable().lookup(".thumb");
        tickLine = (NumberAxis) getSkinnable().lookup(".axis");
        if (tickLine != null) tickLine.setAnimated(false);

        coloredTrack = new StackPane();
        coloredTrack.getStyleClass().add("colored-track");
        coloredTrack.setMouseTransparent(true);

        sliderValue = new Text();
        sliderValue.getStyleClass().setAll("slider-value");

        // Keep the tear-drop square so the 50% background radius stays symmetric. Without
        // this, wide text raises the StackPane's min width above its CSS pref size and the
        // bubble stretches into a skewed ellipse. The side is the text's diagonal so the
        // (rotated-back, horizontal) text fits inside the round part of the bubble.
        animatedThumb = new StackPane() {
            private double squareSide() {
                double w = sliderValue.getLayoutBounds().getWidth();
                double h = sliderValue.getLayoutBounds().getHeight();
                double insets = Math.max(snappedLeftInset() + snappedRightInset(),
                        snappedTopInset() + snappedBottomInset());
                return Math.ceil(Math.hypot(w, h) + insets);
            }

            @Override
            protected double computeMinWidth(double height) {
                return squareSide();
            }

            @Override
            protected double computeMinHeight(double width) {
                return squareSide();
            }
        };
        animatedThumb.getStyleClass().add("animated-thumb");
        animatedThumb.getChildren().add(sliderValue);
        animatedThumb.setMouseTransparent(true);
        animatedThumb.setScaleX(0);
        animatedThumb.setScaleY(0);

        indicatorRoot.getChildren().add(animatedThumb);
        indicatorRoot.setMouseTransparent(true);
        indicatorRoot.setPickOnBounds(false);
        Bindings.bindContent(indicatorRoot.getStyleClass(), slider.getStyleClass());
        indicatorPopup.getContent().add(indicatorRoot);
        indicatorPopup.setAutoFix(false);
        indicatorPopup.setAutoHide(false);
        indicatorPopup.setHideOnEscape(false);

        // Keep the popup glued to (and centered on) the thumb while it is dragged, while
        // it animates, and whenever the bubble resizes because the value text changed.
        thumb.layoutXProperty().addListener(x -> layoutIndicator());
        thumb.layoutYProperty().addListener(y -> layoutIndicator());
        indicatorProgress.addListener(p -> layoutIndicator());
        animatedThumb.layoutBoundsProperty().addListener(b -> layoutIndicator());

        addJFXChildren();
        getChildren().addListener((ListChangeListener<Node>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(added -> {
                        if (added instanceof NumberAxis) {
                            tickLine = (NumberAxis) added;
                            tickLine.setAnimated(false);
                        }
                    });
                }
            }
        });
        registerChangeListener(slider.showTickMarksProperty(), e -> addJFXChildren());
        registerChangeListener(slider.showTickLabelsProperty(), e -> addJFXChildren());
        registerChangeListener(slider.valueFactoryProperty(), obs->refreshSliderValueBinding());
        // Never leave an orphaned popup if the slider leaves the scene
        registerChangeListener(slider.sceneProperty(), obs -> hideIndicator());

        initListeners();
    }

    private void addJFXChildren() {
        ObservableList<Node> children = getChildren();
        Slider slider = getSkinnable();
        if ((slider.isShowTickMarks() || slider.isShowTickLabels()) && tickLine != null && !children.contains(tickLine)) {
            children.add(0, tickLine);
        }
        if (children.contains(coloredTrack)) return;
        children.add(children.indexOf(thumb), coloredTrack);
        children.add(0, mouseHandlerPane);
    }

    private void refreshSliderValueBinding() {
        sliderValue.textProperty().unbind();
        if (((JFXSlider) getSkinnable()).getValueFactory() != null) {
            sliderValue.textProperty()
                    .bind(((JFXSlider) getSkinnable()).getValueFactory().call((JFXSlider) getSkinnable()));
        } else {
            sliderValue.textProperty().bind(Bindings.createStringBinding(() -> {
                if (getSkinnable().getLabelFormatter() != null) {
                    return getSkinnable().getLabelFormatter().toString(getSkinnable().getValue());
                } else {
                    return String.valueOf(Math.round(getSkinnable().getValue()));
                }
            }, getSkinnable().valueProperty()));
        }
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        super.layoutChildren(x, y, w, h);

        boolean horizontal = getSkinnable().getOrientation() == Orientation.HORIZONTAL;
        double width, height, layoutX, layoutY;
        if (horizontal) {
            width = thumb.getLayoutX() - snappedLeftInset();
            height = track.getHeight();
            layoutX = track.getLayoutX();
            layoutY = track.getLayoutY();
        } else {
            height = track.getLayoutBounds().getMaxY() + track.getLayoutY() - thumb.getLayoutY() - snappedBottomInset();
            width = track.getWidth();
            layoutX = track.getLayoutX();
            layoutY = thumb.getLayoutY();
        }

        coloredTrack.resizeRelocate(layoutX, layoutY, width, height);
        mouseHandlerPane.resizeRelocate(x, y, w, h);
    }


    private void initializeVariables() {
        horizontalRotation = 0;
        indicatorRotation = 0;
        if (getSkinnable().getOrientation() != Orientation.HORIZONTAL) {
            horizontalRotation = -90;
        }
        if (((JFXSlider) getSkinnable()).getIndicatorPosition() != JFXSlider.IndicatorPosition.LEFT) {
            indicatorRotation = 180;
        }
        final double rotationAngle = 45;
        sliderValue.setRotate(rotationAngle + indicatorRotation + 3 * horizontalRotation);
        animatedThumb.setRotate(-rotationAngle + indicatorRotation + horizontalRotation);
    }

    private void initListeners() {
        // delegate slider mouse events to track node
        mouseHandlerPane.setOnMousePressed(this::delegateToTrack);
        mouseHandlerPane.setOnMouseReleased(this::delegateToTrack);
        mouseHandlerPane.setOnMouseDragged(this::delegateToTrack);

        // animate value node
        track.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> showIndicator());
        track.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> retractIndicator());
        thumb.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> showIndicator());
        thumb.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> retractIndicator());

        refreshSliderValueBinding();
        updateValueStyleClass();

        getSkinnable().valueProperty().addListener(observable -> updateValueStyleClass());
    }

    private void delegateToTrack(MouseEvent event) {
        if (!event.isConsumed()) {
            event.consume();
            track.fireEvent(event);
        }
    }

    private void updateValueStyleClass() {
        boolean atMin = getSkinnable().getMin() == getSkinnable().getValue();
        boolean atMax = getSkinnable().getMax() == getSkinnable().getValue();
        getSkinnable().pseudoClassStateChanged(MIN_VALUE, atMin);
        getSkinnable().pseudoClassStateChanged(MAX_VALUE, atMax);
        // The indicator lives in a separate popup scene, so mirror the state for CSS
        indicatorRoot.pseudoClassStateChanged(MIN_VALUE, atMin);
        indicatorRoot.pseudoClassStateChanged(MAX_VALUE, atMax);
    }


    /**
     * Shows the indicator popup next to the thumb and animates it out.
     */
    private void showIndicator() {
        Slider slider = getSkinnable();
        if (slider.getScene() == null || slider.getScene().getWindow() == null) {
            return;
        }
        syncIndicatorStyle();
        initializeVariables();
        if (!indicatorPopup.isShowing()) {
            Point2D center = thumbScreenCenter();
            if (center == null) {
                return;
            }
            indicatorPopup.show(slider, center.getX(), center.getY());
        }
        // CSS (padding, font size) can only be applied once the popup's scene exists,
        // so measure and position the bubble after showing it.
        indicatorRoot.applyCss();
        layoutIndicator();
        if (timeline == null) {
            initAnimation();
        }
        timeline.setRate(1);
        timeline.play();
    }

    /**
     * Animates the indicator back into the thumb. The popup is hidden when the animation finishes.
     */
    private void retractIndicator() {
        if (timeline == null) {
            hideIndicator();
            return;
        }
        timeline.setRate(-1);
        timeline.play();
    }

    private void hideIndicator() {
        if (timeline != null) {
            timeline.stop();
        }
        indicatorProgress.set(0);
        animatedThumb.setScaleX(0);
        animatedThumb.setScaleY(0);
        indicatorPopup.hide();
    }

    private Point2D thumbScreenCenter() {
        return thumb.localToScreen(thumb.getWidth() / 2, thumb.getHeight() / 2);
    }

    /**
     * Sizes the popup root so that its center sits on the thumb center, then places the
     * bubble along the animation axis according to {@link #indicatorProgress}. Everything
     * is derived from the bubble's <i>current</i> size, so the bubble stays centered on
     * the thumb even when the value text (and therefore the bubble) grows or shrinks.
     * <p>
     * At progress 0 the bubble's center sits on the thumb's edge; at progress 1 there is a
     * gap of one thumb-size between the thumb and the bubble.
     */
    private void layoutIndicator() {
        if (layingOutIndicator || !indicatorPopup.isShowing()) {
            return;
        }
        layingOutIndicator = true;
        try {
            doLayoutIndicator();
        } finally {
            layingOutIndicator = false;
        }
    }

    private void doLayoutIndicator() {
        Point2D center = thumbScreenCenter();
        if (center == null) {
            return;
        }
        boolean horizontal = getSkinnable().getOrientation() == Orientation.HORIZONTAL;
        boolean right = ((JFXSlider) getSkinnable()).getIndicatorPosition() == JFXSlider.IndicatorPosition.RIGHT;

        // The bubble is kept square by animatedThumb's min-size override
        double bubble = animatedThumb.prefWidth(-1);
        animatedThumb.resize(bubble, bubble);
        double thumbSize = horizontal ? thumb.getHeight() : thumb.getWidth();
        // The bubble is rotated 45 degrees, so pad to keep its corners from being clipped
        double pad = bubble / 2;

        // Distance from the thumb center to the bubble center along the animation axis
        double p = indicatorProgress.get();
        double distance = thumbSize / 2 + p * (thumbSize + bubble / 2);
        double sign = right ? 1 : -1;

        double halfAxis = thumbSize * 1.5 + bubble + pad;   // reach of a fully extended bubble
        double axisLength = 2 * halfAxis;
        double crossLength = bubble + 2 * pad;

        double bubbleAxisPos = halfAxis + sign * distance - bubble / 2;
        double rootWidth, rootHeight;
        if (horizontal) {
            rootWidth = crossLength;
            rootHeight = axisLength;
            animatedThumb.relocate(pad, bubbleAxisPos);
        } else {
            rootWidth = axisLength;
            rootHeight = crossLength;
            animatedThumb.relocate(bubbleAxisPos, pad);
        }
        indicatorRoot.setMinSize(rootWidth, rootHeight);
        indicatorRoot.setPrefSize(rootWidth, rootHeight);
        indicatorRoot.setMaxSize(rootWidth, rootHeight);

        indicatorPopup.setAnchorX(center.getX() - rootWidth / 2);
        indicatorPopup.setAnchorY(center.getY() - rootHeight / 2);
    }

    /**
     * The popup's scene is not a descendant of the slider, so copy the stylesheets
     * and inline style that would otherwise have been inherited.
     */
    private void syncIndicatorStyle() {
        Slider slider = getSkinnable();
        List<String> stylesheets = new ArrayList<>();
        stylesheets.add(slider.getUserAgentStylesheet());
        if (slider.getScene() != null) {
            stylesheets.addAll(slider.getScene().getStylesheets());
        }
        List<Parent> ancestors = new ArrayList<>();
        for (Parent p = slider.getParent(); p != null; p = p.getParent()) {
            ancestors.add(0, p);
        }
        ancestors.forEach(p -> stylesheets.addAll(p.getStylesheets()));
        stylesheets.addAll(slider.getStylesheets());
        if (!indicatorRoot.getStylesheets().equals(stylesheets)) {
            indicatorRoot.getStylesheets().setAll(stylesheets);
        }
        indicatorRoot.setStyle(slider.getStyle());
    }

    /**
     * Builds the show/hide timeline. Position is driven by {@link #indicatorProgress}
     * (see {@link #layoutIndicator()}) so the animation is independent of the bubble size.
     */
    private void initAnimation() {
        clearAnimation();
        timeline = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(animatedThumb.scaleXProperty(), 0, Interpolator.EASE_BOTH),
                        new KeyValue(animatedThumb.scaleYProperty(), 0, Interpolator.EASE_BOTH),
                        new KeyValue(indicatorProgress, 0, Interpolator.EASE_BOTH)),
                new KeyFrame(
                        Duration.seconds(0.2),
                        new KeyValue(animatedThumb.scaleXProperty(), 1, Interpolator.EASE_BOTH),
                        new KeyValue(animatedThumb.scaleYProperty(), 1, Interpolator.EASE_BOTH),
                        new KeyValue(indicatorProgress, 1, Interpolator.EASE_BOTH)));
        Timeline t = timeline;
        timeline.setOnFinished(e -> {
            if (t.getRate() < 0) {
                indicatorPopup.hide();
            }
        });
    }

    @Override
    public void dispose() {
        hideIndicator();
        Bindings.unbindContent(indicatorRoot.getStyleClass(), getSkinnable().getStyleClass());
        super.dispose();
        clearAnimation();
    }

    private void clearAnimation() {
        if (timeline != null) {
            timeline.stop();
            timeline.getKeyFrames().clear();
            timeline = null;
        }
    }
}
