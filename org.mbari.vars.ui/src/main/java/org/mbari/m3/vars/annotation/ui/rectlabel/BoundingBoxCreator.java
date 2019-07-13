package org.mbari.m3.vars.annotation.ui.rectlabel;

import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.mbari.m3.vars.annotation.EventBus;

/**
 * Usage:
 * <pre>
 *     BoundingBoxCreater creator = new BoundingBoxCreator(anchorPane, eventBus)
 *     eventBus.toObservable()
 *         .ofType(BoundingBoxCreatedEvent.class)
 *         .subscribe(bb -> // Do something);
 *
 *     // Explicitly enable it
 *     create.setDisable(false);
 * </pre>
 * @author Brian Schlining
 * @since 2018-05-08T16:18:00
 */
public class BoundingBoxCreator {

    private final EventBus eventBus;
    private final AnchorPane anchorPane;
    private Rectangle rectangle;
    private boolean isBeingDrawn = false;
    private boolean disable = true;
    private double startX;
    private double startY;

    public BoundingBoxCreator(AnchorPane anchorPane, EventBus eventBus) {
        this.anchorPane = anchorPane;
        this.eventBus = eventBus;
        anchorPane.setOnMousePressed(evt -> {
            if (!disable && !isBeingDrawn) {
                startX = evt.getX();
                startY = evt.getY();
                rectangle = new Rectangle();
                rectangle.setFill(Color.color(1, 1, 1, .1));
                rectangle.setStroke(Color.WHITE);
                anchorPane.getChildren().add(rectangle);
                isBeingDrawn = true;
            }
        });
        anchorPane.setOnMouseDragged(evt -> {
            if (isBeingDrawn) {
                double endX = evt.getX();
                double endY = evt.getY();
                modifyRectangle(startX, startY, endX, endY, rectangle);
            }
        });
        anchorPane.setOnMouseReleased(evt -> {
            if (isBeingDrawn) {
                anchorPane.getChildren().remove(rectangle);
                // Fire event
                eventBus.send(new BoundingBoxCreatedEvent(anchorPane, rectangle));
                rectangle = null;
                isBeingDrawn = false;
            }
        });
    }

    public boolean isDisable() {
        return disable;
    }

    public void setDisable(boolean disable) {
        this.disable = disable;
    }

    private void modifyRectangle(double startX,
                                 double startY,
                                 double endX,
                                 double endY,
                                 Rectangle r) {
        r.setX(startX);
        r.setY(startY);
        r.setWidth(endX - startX);
        r.setHeight(endY - startY);

        if (r.getWidth() < 0) {
            r.setWidth(-r.getWidth());
            r.setX(r.getX() - r.getWidth());
        }

        if (r.getHeight() < 0) {
            r.setHeight(-r.getHeight());
            r.setY(r.getY() - r.getHeight());
        }
    }
}
