package org.mbari.m3.vars.annotation.ui.imagepane;

import com.jfoenix.controls.JFXButton;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.ui.imageanno.ImageAnnotationStageController;

public class ImageAnnotationPaneControllerDemo extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        ImageAnnotationStageController controller = new ImageAnnotationStageController(Initializer.getToolBox());

        Button button = new JFXButton("Show Image Annotation Stage");
        button.setOnAction(v -> controller.getStage().show());
        BorderPane pane = new BorderPane(button);
        Scene scene = new Scene(pane);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(v -> System.exit(0));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
