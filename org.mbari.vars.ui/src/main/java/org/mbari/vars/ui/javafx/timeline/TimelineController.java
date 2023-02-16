package org.mbari.vars.ui.javafx.timeline;

import io.reactivex.rxjava3.disposables.Disposable;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Media;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.events.*;
import org.mbari.vars.ui.mediaplayers.MediaPlayer;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;

public class TimelineController {

    public static final int OFFSET = 10;


    private final UIToolBox toolBox;
    private Pane root;
    DoubleProperty endXProperty = new SimpleDoubleProperty();
    DoubleProperty numberOfMinutesProperty = new SimpleDoubleProperty();
    DoubleBinding distanceBetweenMinutesProperty;
    DoubleProperty currentTimeProperty = new SimpleDoubleProperty();
    Line currentTimeTick;
    Line horizontalAxis;
    List<DisplayedAnnotation> displayedAnno = new ArrayList<>();
    private Timeline timeline;
    private volatile Disposable videoIndexDisposable;

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

        eventBus.ofType(MediaPlayerChangedEvent.class)
                .subscribe(evt -> setMediaPlayer(evt.get()));

    }

    private void init() {
        root = new Pane();
        endXProperty.bind(root.widthProperty().subtract(OFFSET));
        horizontalAxis = new Line(OFFSET, OFFSET * 3, endXProperty.get(), OFFSET * 3);
        horizontalAxis.endXProperty().bind(endXProperty);
        horizontalAxis.setStyle("-fx-stroke: #B3A9A3; -fx-stroke-width: 3px;");
        root.getChildren().add(horizontalAxis);
        distanceBetweenMinutesProperty = horizontalAxis.endXProperty()
                .subtract(horizontalAxis.startXProperty())
                .divide(numberOfMinutesProperty);

        currentTimeTick = new Line();
        currentTimeTick.startYProperty().bind(horizontalAxis.startYProperty().subtract(OFFSET));
        currentTimeTick.endYProperty().bind(horizontalAxis.startYProperty().add(OFFSET));
        currentTimeTick.setStyle("-fx-stroke: #FF6663; -fx-stroke-width: 5px;");
        currentTimeTick.startXProperty().bind(currentTimeProperty);
        currentTimeTick.endXProperty().bind(currentTimeProperty);
        currentTimeTick.setStrokeLineCap(StrokeLineCap.ROUND);


    }

    public Pane getRoot() {
        return root;
    }

    private void setMediaPlayer(MediaPlayer<?, ?> mediaPlayer) {
        if (videoIndexDisposable != null) {
            videoIndexDisposable.dispose();
        }
        if (mediaPlayer == null) {
            // TODO remove red tick
            Platform.runLater(() -> root.getChildren().remove(currentTimeTick));
        }
        else {
            if (currentTimeTick.getParent() == null) {
                Platform.runLater(() -> root.getChildren().add(currentTimeTick));
            }
            videoIndexDisposable = mediaPlayer.getVideoIO()
                    .getIndexObservable()
                    .subscribe(videoIndex -> {
                        if (videoIndex != null) {
                            videoIndex.getElapsedTime()
                                    .ifPresent(et ->  {
                                        // TODO update red tick
                                        var etMinutes = et.toMillis() / 1000D / 60D;
                                        var currentTimeX = etMinutes * distanceBetweenMinutesProperty.get() + OFFSET;
                                        Platform.runLater(() -> currentTimeProperty.set(currentTimeX));
                                    });
                        }
                    });
        }
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
        Platform.runLater(() -> {
            displayedAnno.forEach(d -> d.removeFrom(root));
            displayedAnno.clear();
            if (media == null || media.getDuration() == null) {
                if (timeline != null) {
                    timeline.removeFrom(root);
                }
            } else {
                var numberOfMinutes = Math.ceil(media.getDuration().toMillis() / 1000D / 60D);
                numberOfMinutesProperty.set(numberOfMinutes);
                var stride = numberOfMinutes <= 30 ? 1 : (int) Math.round(numberOfMinutes / 30D);

                var tickMarks = new ArrayList<TickMark>();
                for (int i = 0; i <= numberOfMinutes; i += stride) {
                    tickMarks.add(new TickMark(i, new Line(), new Label()));
                }
                timeline = new Timeline(media, tickMarks);
                timeline.addTo(root, horizontalAxis, distanceBetweenMinutesProperty);
            }
        });
    }


}
