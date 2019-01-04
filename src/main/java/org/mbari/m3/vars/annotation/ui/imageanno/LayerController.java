package org.mbari.m3.vars.annotation.ui.imageanno;

import com.jfoenix.controls.JFXToggleNode;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
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

    /**
     *
     * @return The graphic to be displayed on the toggle button that
     *  enables/disables this layer controller
     */
    Text getToggleGraphic();

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
     * Registers this controller with the master {@link ImageAnnotationPaneController}
     * @param controller The master controller
     * @param toggleGroup Used to associated the toogle button that enables/disables this
     *                    layer.
     */
    default void register(ImageAnnotationPaneController controller, ToggleGroup toggleGroup) {
        controller.layerControllers.add(this);
        ToggleButton toggleButton = new JFXToggleNode();
        toggleButton.setToggleGroup(toggleGroup);
        toggleButton.setGraphic(getToggleGraphic());
        toggleButton.selectedProperty().addListener((obs, oldv, newv) -> {
            setDisable(!newv);
            if (newv) {
                controller.getRoot().setBottom(getToolBar());
            }
        });
        controller.getToolbar().getItems().add(toggleButton);
    }

}
