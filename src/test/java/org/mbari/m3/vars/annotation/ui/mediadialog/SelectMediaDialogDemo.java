package org.mbari.m3.vars.annotation.ui.mediadialog;

import com.jfoenix.controls.JFXButton;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.mbari.m3.vars.annotation.model.Authorization;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.services.BasicJWTAuthService;
import org.mbari.m3.vars.annotation.services.MediaService;
import org.mbari.m3.vars.annotation.services.vampiresquid.v1.VamService;
import org.mbari.m3.vars.annotation.services.vampiresquid.v1.VamWebServiceFactory;

import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * @author Brian Schlining
 * @since 2017-06-01T15:12:00
 */
public class SelectMediaDialogDemo extends Application {

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

        Label label = new Label();
        Button button = new JFXButton("Browse");
        Dialog<Media> dialog = new SelectMediaDialog(mediaService, uiBundle);
        button.setOnAction(e -> {
            Optional<Media> media = dialog.showAndWait();
            media.ifPresent(m -> label.setText(m.getUri().toString()));
        });

        VBox vBox = new VBox(label, button);
        Scene scene = new Scene(vBox, 400, 200);
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });

    }
}
