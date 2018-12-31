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

    AnchorPane getRoot();

    boolean isDisabled();
    void setDisable(boolean disable);

    String getDescription();

    Text getToggleGraphic();



    ToolBar getToolBar();

    void clear();

    void draw(ImageViewExt imageViewExt, List<Association> associations, Color color);


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
