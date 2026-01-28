package org.mbari.vars.ui.javafx.timeline;

import javafx.beans.binding.DoubleBinding;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import org.mbari.vars.services.model.Media;

import java.util.List;

record Timeline(Media media, List<TickMark> tickMarks) {
    public void addTo(Pane parent, Line horizontalAxis, DoubleBinding distanceBetweenMinutes) {
        tickMarks.forEach(t -> t.addTo(parent, horizontalAxis, distanceBetweenMinutes));
    }

    public void removeFrom(Pane parent) {
        tickMarks.forEach(t -> t.removeFrom(parent));
    }
}
