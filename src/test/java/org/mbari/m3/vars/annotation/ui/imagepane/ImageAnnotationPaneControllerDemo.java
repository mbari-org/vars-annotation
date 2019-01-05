package org.mbari.m3.vars.annotation.ui.imagepane;

import com.jfoenix.controls.JFXButton;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.ImageReference;
import org.mbari.m3.vars.annotation.ui.imageanno.ImageAnnotationStageController;
import org.mbari.vcr4j.VideoIndex;

import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

        Annotation annotation = new Annotation("Grimpoteuthis",
                "brian",
                new VideoIndex(Instant.now()),
                UUID.randomUUID());
        ImageReference ir = new ImageReference();
        ir.setUrl(new URL("http://dsg.mbari.org/frameGrabs/Tiburon/images/0791/04_58_06_02.jpg"));
        ir.setFormat("image/jpeg");
        List<ImageReference> images = new ArrayList<>();
        images.add(ir);
        annotation.setImages(images);

        controller.setSelectedAnnotation(annotation);



    }

    public static void main(String[] args) {
        launch(args);
    }
}
