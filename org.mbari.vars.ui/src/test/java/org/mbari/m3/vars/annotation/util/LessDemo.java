package org.mbari.m3.vars.annotation.util;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.mbari.vars.services.util.LessCSSLoader;

/**
 * @author Brian Schlining
 * @since 2017-06-26T08:37:00
 */
public class LessDemo extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            Group root = new Group();
            root.getChildren().add(new Rectangle(100,100,200,200));

            Text t = new Text("JavaFX");
            t.getStyleClass().add("javafx");
            t.relocate(150, 320);
            root.getChildren().add(t);

            Scene scene = new Scene(root,400,400);

            LessCSSLoader ls = new LessCSSLoader();
            scene.getStylesheets().add(ls.loadLess(getClass().getResource("/less/sample.less")).toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
