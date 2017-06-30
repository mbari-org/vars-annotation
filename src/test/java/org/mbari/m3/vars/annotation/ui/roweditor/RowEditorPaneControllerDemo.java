package org.mbari.m3.vars.annotation.ui.roweditor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Pair;

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
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
