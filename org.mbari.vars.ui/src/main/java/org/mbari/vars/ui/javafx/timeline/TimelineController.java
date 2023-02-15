package org.mbari.vars.ui.javafx.timeline;

import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Media;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.events.AnnotationsAddedEvent;
import org.mbari.vars.ui.events.AnnotationsChangedEvent;
import org.mbari.vars.ui.events.AnnotationsRemovedEvent;
import org.mbari.vars.ui.events.MediaChangedEvent;

import java.util.ArrayList;
import java.util.List;

public class TimelineController {

    public static final int OFFSET = 10;


    private final UIToolBox toolBox;
    private Pane root;
    DoubleProperty endXProperty = new SimpleDoubleProperty();
    DoubleProperty numberOfMinutesProperty = new SimpleDoubleProperty();
    DoubleBinding distanceBetweenMinutesProperty;
    Line horizontalAxis;
    List<DisplayedAnnotation> displayedAnno = new ArrayList<>();
    private Timeline timeline;

    public TimelineController(UIToolBox toolBox) {
        this.toolBox = toolBox;
        init();
        var eventBus = toolBox.getEventBus().toObserverable();

        eventBus.ofType(MediaChangedEvent.class)
                .subscribe(e -> setMedia(e.get()));

        eventBus.ofType(AnnotationsAddedEvent.class)
                .subscribe(e ->  e.get().forEach(this::addAnnotation));

        eventBus.ofType(AnnotationsRemovedEvent.class)
                .subscribe(e -> e.get().forEach(this::removeAnnotation));

        eventBus.ofType(AnnotationsChangedEvent.class)
                .subscribe(e -> e.get().forEach(a -> {
                    removeAnnotation(a);
                    addAnnotation(a);
                }));

    }

    private void init() {
        root = new Pane();
        endXProperty.bind(root.widthProperty().subtract(OFFSET));
        horizontalAxis = new Line(OFFSET, OFFSET * 3, endXProperty.get(), OFFSET * 3);
        horizontalAxis.endXProperty().bind(endXProperty);
        horizontalAxis.setStyle("-fx-stroke: #B3A9A3; -fx-stroke-width: 2px;");
        root.getChildren().add(horizontalAxis);
        distanceBetweenMinutesProperty = horizontalAxis.endXProperty()
                .subtract(horizontalAxis.startXProperty())
                .divide(numberOfMinutesProperty);

    }

    public Pane getRoot() {
        return root;
    }

    private void addAnnotation(Annotation a) {
        Platform.runLater(() -> {
            var da = new DisplayedAnnotation(a, new Label(), new Line());
            displayedAnno.add(da);
            da.addTo(root, horizontalAxis, distanceBetweenMinutesProperty, toolBox.getEventBus());
        });
    }

    private void removeAnnotation(Annotation a) {
        Platform.runLater(() -> {
            for (int i = 0; i < displayedAnno.size(); i++) {
                var d = displayedAnno.get(i);
                if (d.annotation().getObservationUuid().equals(a.getObservationUuid())) {
                    displayedAnno.remove(i);
                    d.removeFrom(root);
                    break;
                }
            }
        });
    }



    private void setMedia(Media media) {
        displayedAnno.forEach(d -> d.removeFrom(root));
        displayedAnno.clear();
        if (media == null || media.getDuration() == null) {
            if (timeline != null) {
                timeline.removeFrom(root);
            }
        }
        else {
            var numberOfMinutes = Math.ceil(media.getDuration().toMillis() / 1000D / 60D);
            numberOfMinutesProperty.set(numberOfMinutes);

            var tickMarks = new ArrayList<TickMark>();
            for (int i = 0; i <= numberOfMinutes; i++) {
                tickMarks.add(new TickMark(i, new Line(), new Label()));
            }
            timeline = new Timeline(media, tickMarks);
            timeline.addTo(root, horizontalAxis, distanceBetweenMinutesProperty);
        }
    }


}
