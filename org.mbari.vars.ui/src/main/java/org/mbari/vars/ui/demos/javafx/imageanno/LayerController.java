package org.mbari.vars.ui.demos.javafx.imageanno;

import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Image;

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

    void select(Annotation annotation, Image image);

    ToolBar getToolBar();


}
