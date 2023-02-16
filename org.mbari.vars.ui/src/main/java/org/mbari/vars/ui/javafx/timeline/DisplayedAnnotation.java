package org.mbari.vars.ui.javafx.timeline;

import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import org.mbari.vars.core.EventBus;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.ui.Initializer;
import org.mbari.vars.ui.events.AnnotationsSelectedEvent;
import org.mbari.vars.ui.messages.SeekMsg;
import org.mbari.vars.ui.util.ColorUtil;

import java.time.Duration;
import java.util.List;

record DisplayedAnnotation(Annotation annotation, Label label, Line line) {


    public void addTo(Pane parent,
                      Line horizontalAxis,
                      DoubleBinding distanceBetweenMinutes,
                      EventBus eventBus) {

        var media = Initializer.getToolBox().getData().getMedia();
        if (media == null) {
            return;
        }
        var elapsedTimeOpt = media.toMediaElapsedTime(annotation);
        if (elapsedTimeOpt.isEmpty()) {
            return;
        }
        var elapsedTime = elapsedTimeOpt.get();

        Platform.runLater(() -> {
            var minutes = elapsedTime.toMillis() / 1000.0 / 60.0;
            var xProp = distanceBetweenMinutes.multiply(minutes).add(TimelineController.OFFSET);

            // --- LABEL
            var firstLetter = annotation.getConcept().toUpperCase().charAt(0);
            var charCode = (int) firstLetter;
            var shortName = firstLetter + "";

            label.getStylesheets().clear();
            label.setText(shortName);
            label.layoutXProperty().bind(xProp.subtract(label.widthProperty().divide(2)));
            var incremProp = parent.heightProperty()
                    .subtract(TimelineController.OFFSET * 2)
                    .subtract(horizontalAxis.startYProperty())
                    .divide(26)
                    .multiply(charCode - 65)
                    .add(horizontalAxis.startYProperty());
            label.layoutYProperty().bind(incremProp);

            var fillHex = ColorUtil.stringToHexColor(annotation.getConcept(), 0.7);
            var fill = Color.web(fillHex, 0.7);

            label.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: " + fillHex + ";");

            label.setOnMouseClicked(evt -> {
                var e = new AnnotationsSelectedEvent(DisplayedAnnotation.class, List.of(annotation));
                eventBus.send(e);
                if (evt.getClickCount() == 2) {
//                    var media = Initializer.getToolBox().getData().getMedia();
                    SeekMsg.seek(media, annotation, eventBus);
                }
            });

            if (annotation.getRecordedTimestamp() != null) {
                label.setTooltip(new Tooltip(annotation.getConcept() + " at " + annotation.getRecordedTimestamp()));
            }
            else {
                label.setTooltip(new Tooltip(annotation.getConcept() + " at " + minutes + " minutes"));
            }

            // -- LINE
            line.startXProperty().bind(xProp);
            line.endXProperty().bind(xProp);
            line.startYProperty().bind(horizontalAxis.startYProperty());
            line.endYProperty().bind(label.layoutYProperty());

            var lightStroke = new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), 0.15);
            var heavyStroke = new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), 1);
            var heavyStrokeHex = ColorUtil.toHex(heavyStroke);
            line.setStroke(lightStroke);

            label.setOnMouseEntered(evt -> Platform.runLater(() -> {
                line.setStroke(heavyStroke);
                line.setStrokeWidth(3);
                label.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: " + heavyStrokeHex + ";");
                label.setText(annotation.getConcept());
            }));

            label.setOnMouseExited(evt -> Platform.runLater(() -> {
                line.setStroke(lightStroke);
                line.setStrokeWidth(3);
                label.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: " + fillHex + ";");
                label.setText(shortName);
            }));

            parent.getChildren().addAll(line, label);
        });

    }

    public void removeFrom(Pane parent) {
        Platform.runLater(() -> {
            label.layoutXProperty().unbind();
            label.layoutYProperty().unbind();
            line.startXProperty().unbind();
            line.startYProperty().unbind();
            line.endXProperty().unbind();
            line.endYProperty().unbind();
            label.setOnMouseEntered(e -> {});
            label.setOnMouseExited(e -> {});
            label.setOnMouseClicked(e -> {});
            parent.getChildren().removeAll(label, line);
            parent.requestLayout();
        });

    }
}
