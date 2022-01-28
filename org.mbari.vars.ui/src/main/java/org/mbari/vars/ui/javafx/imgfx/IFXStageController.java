package org.mbari.vars.ui.javafx.imgfx;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.mbari.vars.core.EventBus;
import org.mbari.vars.ui.UIToolBox;

public class IFXStageController {

    private final UIToolBox toolBox;
    private EventBus varsEventBUs;
    private final IFXData data = new IFXData();
    private Stage stage;

    public IFXStageController(UIToolBox toolBox) {
        this.toolBox = toolBox;
    }

    public Stage getStage() {
        if (stage == null) {
            stage = new Stage();
//            BorderPane root = rectLabelController.getRoot();
//            Scene scene = new Scene(root);
//            scene.getStylesheets().addAll(toolBox.getStylesheets());
//            stage.setScene(scene);
        }
        return stage;
    }

}
