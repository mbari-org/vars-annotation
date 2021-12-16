package org.mbari.vars.ui.demos.javafx.mediadialog;

import com.jfoenix.controls.JFXButton;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.mbari.vars.ui.demos.javafx.DemoConstants;
import org.mbari.vars.services.AnnotationService;
import org.mbari.vars.services.MediaService;
import org.mbari.vars.services.model.Media;
import org.mbari.vars.ui.javafx.mediadialog.SelectMediaDialog;

import java.util.Optional;
import java.util.ResourceBundle;

/**
 * @author Brian Schlining
 * @since 2017-06-01T15:12:00
 */
public class SelectMediaDialogDemo extends Application {

    private static ResourceBundle uiBundle = DemoConstants.UI_BUNDLE;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        MediaService mediaService = DemoConstants.newMediaService();
        AnnotationService annotationService = DemoConstants.newAnnotationService();

        Label label = new Label();
        Button button = new JFXButton("Browse");
        Dialog<Media> dialog = new SelectMediaDialog(DemoConstants.getToolBox(), uiBundle);
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
