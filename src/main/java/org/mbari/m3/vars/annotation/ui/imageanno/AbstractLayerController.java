package org.mbari.m3.vars.annotation.ui.imageanno;

import com.jfoenix.controls.JFXToggleNode;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.mbari.m3.vars.annotation.UIToolBox;

public abstract class AbstractLayerController implements LayerController {

    private AnchorPane anchorPane;
    private ToggleButton enableButton;
    private Data data;
    private UIToolBox toolBox;


    public AbstractLayerController(UIToolBox toolBox, Data data) {
        this.toolBox = toolBox;
        this.data = data;
    }

    public abstract Node getEnableButtonGraphic();

    @Override
    public ToggleButton getEnableButton() {
        if (enableButton == null) {
            enableButton = new JFXToggleNode();
            enableButton.setGraphic(getEnableButtonGraphic());
        }
        return enableButton;
    }

    @Override
    public AnchorPane getRoot() {
        if (anchorPane == null) {
            anchorPane = new AnchorPane();
            // Bind our anchorPane to be the same size as the stackPane that we layer on top of
            StackPane stackPane = data.getStackPane();
            anchorPane.prefHeightProperty().bind(stackPane.heightProperty());
            anchorPane.prefWidthProperty().bind(stackPane.widthProperty());
        }
        return anchorPane;
    }

    protected Data getData() {
        return data;
    }

    protected UIToolBox getToolBox() {
        return toolBox;
    }


}
