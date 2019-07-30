package org.mbari.vars.ui.javafx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.mbari.vars.ui.Initializer;
import org.mbari.vars.services.model.Annotation;

/**
 * @author Brian Schlining
 * @since 2017-08-07T17:11:00
 */
public class ImageViewControllerDemo  extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        ImageViewController controller = new ImageViewController(Initializer.getToolBox());
        Annotation annotation = DemoConstants.newTestAnnotation();
        controller.setAnnotation(annotation);
        Scene scene = new Scene(controller.getRoot());
        primaryStage.setScene(scene);
        primaryStage.show();

    }
}
