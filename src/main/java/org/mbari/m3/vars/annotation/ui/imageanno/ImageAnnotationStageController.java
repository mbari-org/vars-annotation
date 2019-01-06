package org.mbari.m3.vars.annotation.ui.imageanno;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.ImageReference;

public class ImageAnnotationStageController {

    private final UIToolBox toolBox;
    private Stage stage;
    private final ImageAnnotationPaneController paneController;

    public ImageAnnotationStageController(UIToolBox toolBox) {
        this.toolBox = toolBox;
        paneController = ImageAnnotationPaneController.newInstance(toolBox);
    }

    public void setSelectedAnnotation(final Annotation annotation) {
        paneController.setSelectedAnnotation(annotation);
    }

    public ImageAnnotationPaneController getPaneController() {
        return paneController;
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
