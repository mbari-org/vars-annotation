package org.mbari.vars.ui.demos.javafx.mediadialog;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.mbari.vars.ui.Initializer;
import org.mbari.vars.ui.demos.javafx.DemoConstants;
import org.mbari.vars.services.AnnotationService;
import org.mbari.vars.services.MediaService;
import org.mbari.vars.ui.javafx.mediadialog.VideoBrowserPaneController;

import java.util.ResourceBundle;

/**
 * @author Brian Schlining
 * @since 2017-06-01T11:03:00
 */
public class VideoBrowserPaneDemo extends Application {

    private static ResourceBundle uiBundle = DemoConstants.UI_BUNDLE;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        AnnotationService annotationService = DemoConstants.newAnnotationService();
        MediaService mediaService = DemoConstants.newMediaService();
        VideoBrowserPaneController controller = new VideoBrowserPaneController(DemoConstants.getToolBox(), uiBundle);
        Scene scene = new Scene(controller.getRoot());
        scene.getStylesheets().addAll(Initializer.getToolBox().getStylesheets());
        //scene.getStylesheets().add("/css/common.css");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
