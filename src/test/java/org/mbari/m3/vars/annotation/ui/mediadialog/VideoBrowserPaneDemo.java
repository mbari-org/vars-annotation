package org.mbari.m3.vars.annotation.ui.mediadialog;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.mbari.m3.vars.annotation.model.Authorization;
import org.mbari.m3.vars.annotation.services.BasicJWTAuthService;
import org.mbari.m3.vars.annotation.services.MediaService;
import org.mbari.m3.vars.annotation.services.vampiresquid.v1.VamService;
import org.mbari.m3.vars.annotation.services.vampiresquid.v1.VamWebServiceFactory;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Brian Schlining
 * @since 2017-06-01T11:03:00
 */
public class VideoBrowserPaneDemo extends Application {

    private static ResourceBundle uiBundle = ResourceBundle.getBundle("UIBundle",
            Locale.getDefault());

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Authorization auth = new Authorization("BEARER", "foo");
        VamWebServiceFactory factory = new VamWebServiceFactory("http://m3.shore.mbari.org/vam/v1");
        MediaService mediaService = new VamService(factory,
                new BasicJWTAuthService(factory, auth));

        VideoBrowserPaneController controller = new VideoBrowserPaneController(mediaService, uiBundle);
        Scene scene = new Scene(controller.getRoot());
        scene.getStylesheets().add("/application.css");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
