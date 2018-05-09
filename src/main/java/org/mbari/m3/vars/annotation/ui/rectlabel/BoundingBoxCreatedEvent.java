package org.mbari.m3.vars.annotation.ui.rectlabel;

import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Shape;


/**
 * @author Brian Schlining
 * @since 2018-05-08T16:44:00
 */
public class BoundingBoxCreatedEvent {
    protected final AnchorPane anchorPane;
    protected final Shape shape;

    public BoundingBoxCreatedEvent(AnchorPane anchorPane, Shape shape) {
        this.anchorPane = anchorPane;
        this.shape = shape;
    }

    public AnchorPane getAnchorPane() {
        return anchorPane;
    }

    public Shape getShape() {
        return shape;
    }
}
