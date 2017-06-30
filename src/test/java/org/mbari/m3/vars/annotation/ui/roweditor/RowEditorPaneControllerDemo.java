package org.mbari.m3.vars.annotation.ui.roweditor;

import com.google.common.collect.Lists;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.commands.SelectedAnnotations;
import org.mbari.m3.vars.annotation.model.Annotation;

/**
 * @author Brian Schlining
 * @since 2017-06-29T17:14:00
 */
public class RowEditorPaneControllerDemo extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Pair<Pane, RowEditorPaneController> rowEditor = RowEditorPaneController.newInstance();
        Scene scene = new Scene(rowEditor.getKey());
        scene.getStylesheets().addAll("/css/roweditor.css");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();

        Annotation a = new Annotation();
        a.setConcept("Grimpoteuthis");


        Initializer.getToolBox()
                .getEventBus()
                .send(new SelectedAnnotations(Lists.newArrayList(a)));
    }
}
