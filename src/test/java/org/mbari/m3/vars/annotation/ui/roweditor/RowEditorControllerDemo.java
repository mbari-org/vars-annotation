package org.mbari.m3.vars.annotation.ui.roweditor;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.commands.SelectedAnnotations;
import org.mbari.m3.vars.annotation.model.Annotation;

import java.time.Instant;

/**
 * @author Brian Schlining
 * @since 2017-07-20T08:58:00
 */
public class RowEditorControllerDemo extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        Initializer.getToolBox()
                .getEventBus()
                .toObserverable()
                .subscribe(obj -> System.out.println(obj));

        RowEditorController rowEditor = new RowEditorController();
        Scene scene = new Scene(rowEditor.getRoot());
        scene.getStylesheets().addAll("/css/roweditor.css");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.setWidth(600);
        primaryStage.setHeight(400);
        primaryStage.show();

        Annotation a = new Annotation("Grimpoteuthis", "brian");
        a.setRecordedTimestamp(Instant.now());

        new Thread(() -> {
            try {
                Thread.sleep(4000);
                rowEditor.setAnnotation(a);
                Initializer.getToolBox()
                        .getEventBus()
                        .send(new SelectedAnnotations(a));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();




    }
}
