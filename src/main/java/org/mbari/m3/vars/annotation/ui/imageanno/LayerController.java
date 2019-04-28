package org.mbari.m3.vars.annotation.ui.imageanno;

import com.jfoenix.controls.JFXToggleNode;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.m3.vars.annotation.ui.shared.ImageViewExt;

import java.util.List;

/**
 * 1. Create
 * 2. Provide a toolbar UI for when a mode is selected to enable
 *    this widget
 * 3. Resize this layer when imageview is resized. Nodes should be
 *    scaled appropriately.
 * 4. Create appropriate association
 * 5. Display association if present in annotation
 */
public interface LayerController {

    /**
     * This is the pane that this layer's child nodes are drawn on
     * @return
     */
    AnchorPane getRoot();

    /**
     *
     * @return true if this layer should not be drawn
     */
    boolean isDisabled();

    /**
     *
     * @param disable true to hide this layer, false to draw contents
     */
    void setDisable(boolean disable);


    ToggleButton getEnableButton();

    /**
     *
     * @return A toolbar that is used for configuring/controlling the
     *  behavior of this layer. It will be displayed at the bottom
     *  of the image pane controller
     */
    ToolBar getToolBar();

    /**
     * removes all drawn content from this layer
     */
    void clear();

    /**
     * Draws content over the image view using the provided color. The associations
     * contain the drawing content and are mapped to the shapes drawn. Only
     * associations that match the criteria (linkname) for a specific layer
     * will be drawn. Othere sill just be ignored.
     * @param imageViewExt
     * @param associations
     * @param color
     */
    void draw(ImageViewExt imageViewExt, List<Association> associations, Color color);



    /**
     * Convert a mouse events coordinates to the actual image coordinates. The mouse event
     * is assumed to originate in the stackpane or one of it's anchorpane layers
     * @param event
     * @param imageViewExt
     * @return
     */
    static Point2D toImageCoordinates(MouseEvent event, ImageViewExt imageViewExt) {
        ImageView imageView = imageViewExt.getImageView();
        Bounds bounds = imageView.getBoundsInParent();
        double scale = imageViewExt.computeActualScale();
        // ImageView coords
        double viewX = event.getX() - bounds.getMinX();
        double viewY = event.getY() - bounds.getMinY();

        // Annotation coords (in original image coordinate space)
        double annoX = viewX / scale;
        double annoY = viewY / scale;

        return new Point2D(annoX, annoY);
    }

    static Point2D toImageCoordinates(Point2D point, ImageViewExt imageViewExt) {
        ImageView imageView = imageViewExt.getImageView();
        Bounds bounds = imageView.getBoundsInParent();
        double scale = imageViewExt.computeActualScale();
        // ImageView coords
        double viewX = point.getX() - bounds.getMinX();
        double viewY = point.getY() - bounds.getMinY();

        // Annotation coords (in original image coordinate space)
        double annoX = viewX / scale;
        double annoY = viewY / scale;

        return new Point2D(annoX, annoY);
    }

//    static Shape toImageCoordinates(Shape shape, ImageViewExt) {
//        Scale
//    }


}
