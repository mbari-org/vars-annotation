package org.mbari.m3.vars.annotation.ui.roweditor;

import io.reactivex.Observable;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.events.AnnotationsSelectedEvent;
import org.mbari.m3.vars.annotation.commands.CreateAssociation;
import org.mbari.m3.vars.annotation.model.Annotation;

import java.time.Instant;
import java.util.UUID;

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

        // TODO create new annotation and fire off to servie

        Annotation a = new Annotation("Grimpoteuthis", "brian");
        a.setRecordedTimestamp(Instant.now());
        a.setObservationUuid(UUID.randomUUID());

        new Thread(() -> {
            try {
                Thread.sleep(4000);
                rowEditor.setAnnotation(a);
                Initializer.getToolBox()
                        .getEventBus()
                        .send(new AnnotationsSelectedEvent(a));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

    }

    private void initializeEventHandling() {
        Observable<Object> obs = Initializer.getToolBox().getEventBus().toObserverable();
        obs.ofType(AnnotationsSelectedEvent.class)
                .subscribe(annos -> {

                });

        obs.ofType(CreateAssociation.class)
                .subscribe(ca -> {
                });
    }
}
