package org.mbari.vars.ui.javafx.timeline;

import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;

record TickMark(int minute, Line tick, Label label) {

    public void addTo(Pane parent, Line horizontalAxis, DoubleBinding distanceBetweenMinutes) {
        Platform.runLater(() -> {

            tick.setStrokeLineCap(StrokeLineCap.ROUND);
            var offset = TimelineController.OFFSET;
            tick.startXProperty().bind(distanceBetweenMinutes.multiply(minute).add(offset));
            tick.endXProperty().bind(tick.startXProperty());

            tick.startYProperty().bind(horizontalAxis.startYProperty().subtract(offset));
            tick.endYProperty().bind(horizontalAxis.startYProperty().add(offset));
            tick.setStyle("-fx-stroke: #B3A9A3; -fx-stroke-width: 3px;");

            var label = new Label("" + minute);
            label.setTextFill(Color.valueOf("#B3A9A3"));
            label.layoutXProperty().bind(tick.startXProperty().subtract(label.widthProperty().divide(2)));
            label.layoutYProperty().bind(tick.startYProperty().subtract(offset * 2));
            label.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
            parent.getChildren().addAll(tick, label);
        });
    }

    public void removeFrom(Pane parent) {
        Platform.runLater(() -> {
            parent.getChildren().removeAll(tick, label);
            tick.startXProperty().unbind();
            tick.endXProperty().unbind();
            tick.startYProperty().unbind();
            tick.endYProperty().unbind();
            label.layoutXProperty().unbind();
            label.layoutYProperty().unbind();
        });

    }
}
