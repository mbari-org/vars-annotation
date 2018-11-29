package org.mbari.m3.vars.annotation.ui.shared;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.UIToolBox;

/**
 * @author Brian Schlining
 * @since 2018-11-29T15:19:00
 */
public class DetailEditorPaneDemo extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        UIToolBox toolBox = Initializer.getToolBox();
        DetailEditorPaneController controller = DetailEditorPaneController.newInstance(toolBox);
        GridPane root = controller.getRoot();
        root.setPadding(new Insets(10));
        Scene scene = new Scene(root);
        scene.getStylesheets().addAll(toolBox.getStylesheets());
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
