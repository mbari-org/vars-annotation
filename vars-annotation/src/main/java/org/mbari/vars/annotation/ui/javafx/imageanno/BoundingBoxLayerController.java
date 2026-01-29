package org.mbari.vars.annotation.ui.javafx.imageanno;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Shape;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annosaurus.sdk.r1.models.Association;
import org.mbari.vars.services.model.Image;

public class BoundingBoxLayerController implements LayerController {

    private class BoundingBox {
        double x;
        double y;
        double width;
        double height;
    }

    private class BoundingBoxNode {
        Shape boundingBox;
        ChangeListener<? super Number> changeListener;
        Association association;

        public BoundingBoxNode(Shape boundingBox,
                               ChangeListener<? super Number> changeListener,
                               Association association) {
            this.boundingBox = boundingBox;
            this.changeListener = changeListener;
            this.association = association;
        }
    }

    @Override
    public AnchorPane getRoot() {
        return null;
    }

    @Override
    public void select(Annotation annotation, Image image) {

    }

    @Override
    public ToolBar getToolBar() {
        return null;
    }
}
