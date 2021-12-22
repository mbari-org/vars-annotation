package org.mbari.vars.ui.demos.javafx.shared;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.mbari.vars.ui.Initializer;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.javafx.shared.ConceptSelectionDialogController;

/**
 * @author Brian Schlining
 * @since 2017-09-19T11:16:00
 */
public class ConceptSelectionDialogControllerDemo extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        UIToolBox toolBox = Initializer.getToolBox();
        ConceptSelectionDialogController controller = new ConceptSelectionDialogController(toolBox);
        controller.setConcept(toolBox.getConfig().getString("app.annotation.upon.root"), true);
        Button button = new Button("Show dialog");
        button.setOnAction(e -> controller.getDialog().showAndWait());
        Scene scene = new Scene(button);
        scene.getStylesheets().addAll(toolBox.getStylesheets());
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
