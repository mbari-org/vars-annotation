package org.mbari.vars.annotation.ui.javafx.imageanno;

import javafx.scene.control.ToolBar;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Shape;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annosaurus.sdk.r1.models.Image;
import org.mbari.vars.annotation.ui.javafx.shared.ImageViewExt;

import java.util.ArrayList;
import java.util.List;

public class PointLayerController implements  LayerController {

    private AnchorPane anchorPane;
    private boolean disable = true;
    private ToolBar toolBar;
    private final UIToolBox toolBox;
    private List<LayerNode> points = new ArrayList<>();

    public PointLayerController(UIToolBox toolBox, StackPane stackPane) {
        this.toolBox = toolBox;
        // Bind size tto the pane that contains this anchor pane
        getRoot().prefHeightProperty().bind(stackPane.heightProperty());
        getRoot().prefWidthProperty().bind(stackPane.widthProperty());
    }

    @Override
    public AnchorPane getRoot() {
        if (anchorPane == null) {
            anchorPane = new AnchorPane();
        }
        return null;
    }

    @Override
    public void select(Annotation annotation, Image image) {

    }

    @Override
    public ToolBar getToolBar() {
        if (toolBar == null) {
            toolBar = new ToolBar();

        }
        return null;
    }

    public Shape draw(ImageViewExt imageViewExt) {
        imageViewExt.getImageView();
        return null;
    }
}
