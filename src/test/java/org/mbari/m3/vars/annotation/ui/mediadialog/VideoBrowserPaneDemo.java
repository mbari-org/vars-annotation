package org.mbari.m3.vars.annotation.ui.mediadialog;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.mbari.m3.vars.annotation.services.MediaService;
import org.mbari.m3.vars.annotation.ui.DemoConstants;

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
        MediaService mediaService = DemoConstants.newMediaService();

        VideoBrowserPaneController controller = new VideoBrowserPaneController(mediaService, uiBundle);
        Scene scene = new Scene(controller.getRoot());
        scene.getStylesheets().add("/css/common.css");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
