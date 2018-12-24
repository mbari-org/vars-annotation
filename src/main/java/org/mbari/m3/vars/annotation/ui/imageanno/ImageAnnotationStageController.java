package org.mbari.m3.vars.annotation.ui.imageanno;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.mbari.m3.vars.annotation.UIToolBox;

public class ImageAnnotationStageController {

    private final UIToolBox toolBox;
    private Stage stage;
    private final ImageAnnotationPaneController paneController;

    public ImageAnnotationStageController(UIToolBox toolBox) {
        this.toolBox = toolBox;
        paneController = ImageAnnotationPaneController.newInstance(toolBox);
    }

    public Stage getStage() {
        if (stage == null) {
            stage = new Stage();
            BorderPane root = paneController.getRoot();
            Scene scene = new Scene(root);
            scene.getStylesheets()
                    .addAll(toolBox.getStylesheets());
            stage.setScene(scene);
        }
        return stage;
    }
}
