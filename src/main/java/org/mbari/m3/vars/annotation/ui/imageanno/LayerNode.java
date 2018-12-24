package org.mbari.m3.vars.annotation.ui.imageanno;

import javafx.beans.value.ChangeListener;
import javafx.scene.shape.Shape;
import org.mbari.m3.vars.annotation.model.Association;

public class LayerNode {
    private final Shape shape;
    private final ChangeListener<? super Number> changeListener;
    private final Association association;

    public LayerNode(Shape shape,
                     ChangeListener<? super Number> changeListener,
                     Association association) {
        this.shape = shape;
        this.changeListener = changeListener;
        this.association = association;
    }

    public Shape getShape() {
        return shape;
    }

    public ChangeListener<? super Number> getChangeListener() {
        return changeListener;
    }

    public Association getAssociation() {
        return association;
    }

}
