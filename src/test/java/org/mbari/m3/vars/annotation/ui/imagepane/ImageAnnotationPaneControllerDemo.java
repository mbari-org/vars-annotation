package org.mbari.m3.vars.annotation.ui.imagepane;

import com.jfoenix.controls.JFXButton;
import io.reactivex.Observable;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.commands.Command;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.ImageReference;
import org.mbari.m3.vars.annotation.ui.imageanno.ImageAnnotationStageController;
import org.mbari.m3.vars.annotation.ui.imageanno.LayerController;
import org.mbari.m3.vars.annotation.ui.imageanno.PointLayerController;
import org.mbari.vcr4j.VideoIndex;

import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ImageAnnotationPaneControllerDemo extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        List<String> concepts = Arrays.asList("Nanomia",
                "Grimpoeteuthis", "Aegina", "Raja").stream()
                .sorted()
                .collect(Collectors.toList());

        UIToolBox toolBox = Initializer.getToolBox();
        Observable<Object> observable = toolBox.getEventBus().toObserverable();

        observable.subscribe(System.out::println);
        observable.ofType(Command.class)
                .subscribe(cmd -> System.out.println(cmd.getDescription()));


        ImageAnnotationStageController controller = new ImageAnnotationStageController(toolBox);
        List<LayerController> layerControllers = controller.getPaneController()
                .getLayerControllers();
        layerControllers.add(new PointLayerController(toolBox, controller.getPaneController().getData()));
        layerControllers.stream()
                .filter(c -> c instanceof PointLayerController)
                .map(c -> (PointLayerController) c)
                .forEach(c -> {
                    c.getConceptComboBox()
                            .setItems(FXCollections.observableArrayList(concepts));
                    c.getConceptComboBox()
                            .getSelectionModel()
                            .select(0);
                });


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
        ImageReference ir0 = new ImageReference();
        ir0.setUrl(new URL("http://dsg.mbari.org/frameGrabs/Tiburon/images/0791/04_58_06_02.jpg"));
        ir0.setFormat("image/jpeg");
        ImageReference ir1 = new ImageReference();
        ir1.setUrl(new URL("http://dsg.mbari.org/frameGrabs/Ventana/stills/2003/129/03_41_56_15.jpg"));
        ir1.setDescription("Another image");
        ir1.setFormat("image/jpeg");
        List<ImageReference> images = new ArrayList<>();
        images.add(ir0);
        images.add(ir1);
        annotation.setImages(images);

        controller.setSelectedAnnotation(annotation);



    }

    public static void main(String[] args) {
        launch(args);
    }
}
