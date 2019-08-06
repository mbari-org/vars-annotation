package org.mbari.vars.javafx;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class EditableBoundingBoxDemo1 extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        AnchorPane pane = new AnchorPane();
        BorderPane root = new BorderPane(pane);

        root.setCenter(pane);

        EditableBoundingBox box1 = new EditableBoundingBox(10, 10, 40, 60);
        EditableBoundingBox box2 = new EditableBoundingBox(200, 100, 50, 73);
        box1.setColor(Color.DEEPSKYBLUE);
        pane.getChildren().addAll(box1.getShapes());
        box1.getShapes().forEach(r ->
                r.addEventHandler(MouseEvent.MOUSE_PRESSED, evt -> {
                    pane.getChildren().removeAll(box1.getShapes());
                    pane.getChildren().addAll(0, box1.getShapes());
                    box1.setColor(Color.ORANGE);
                    box2.setColor(Color.DEEPSKYBLUE);
                }));


        box2.setColor(Color.DEEPSKYBLUE);
        pane.getChildren().addAll(box2.getShapes());
        box2.getShapes().forEach(r ->
                r.addEventHandler(MouseEvent.MOUSE_PRESSED, evt -> {
                    pane.getChildren().removeAll(box2.getShapes());
                    pane.getChildren().addAll(0, box2.getShapes());
                    box2.setColor(Color.ORANGE);
                    box1.setColor(Color.DEEPSKYBLUE);
                }));
        Scene scene = new Scene(root);
        primaryStage.setWidth(500);
        primaryStage.setHeight(500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
