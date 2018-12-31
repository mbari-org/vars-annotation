package org.mbari.m3.vars.annotation.ui.imageanno;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.m3.vars.annotation.model.Image;
import org.mbari.m3.vars.annotation.ui.shared.ImageViewExt;

import java.util.List;

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
    public void draw(ImageViewExt imageViewExt, List<Association> associations, Color color) {

    }

    @Override
    public ToolBar getToolBar() {
        return null;
    }

    @Override
    public void clear() {

    }

    @Override
    public void setDisable(boolean disable) {

    }

    @Override
    public boolean isDisabled() {
        return false;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public Text getToggleGraphic() {
        return null;
    }
}
