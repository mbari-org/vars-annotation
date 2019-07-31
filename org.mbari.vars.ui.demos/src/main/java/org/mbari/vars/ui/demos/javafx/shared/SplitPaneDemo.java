package org.mbari.vars.ui.demos.javafx.shared;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * @author Brian Schlining
 * @since 2017-08-28T16:09:00
 */
public class SplitPaneDemo extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        Label left = new Label("foo");
        TextField tf0 = new TextField();
        HBox hBox = new HBox(left, tf0);
        Label right = new Label("bar");
        ListView<String> listView = new ListView<>();
        VBox vBox = new VBox(right, listView);

        TableView<String> tableView = new TableView<>();
        SplitPane rightPane = new SplitPane(vBox, tableView);
        rightPane.setOrientation(Orientation.VERTICAL);

        SplitPane splitPane = new SplitPane(hBox, rightPane);
        Scene scene = new Scene(splitPane);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }
}
