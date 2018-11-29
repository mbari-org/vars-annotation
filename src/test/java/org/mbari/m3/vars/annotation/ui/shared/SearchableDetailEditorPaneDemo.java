package org.mbari.m3.vars.annotation.ui.shared;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.services.ConceptService;

/**
 * @author Brian Schlining
 * @since 2018-11-29T15:14:00
 */
public class SearchableDetailEditorPaneDemo extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        UIToolBox toolBox = Initializer.getToolBox();
        SearchableDetailEditorPaneController controller = SearchableDetailEditorPaneController.newInstance(toolBox);
        VBox root = controller.getRoot();
        root.setPadding(new Insets(10));
        Scene scene = new Scene(root);
        scene.getStylesheets().addAll(toolBox.getStylesheets());
        primaryStage.setScene(scene);
        primaryStage.show();

        ConceptService conceptService = toolBox.getServices().getConceptService();
        conceptService
                .findRoot()
                .thenAccept(concept -> conceptService.findTemplates(concept.getName())
                            .thenAccept(controller::setTemplates));
    }
}
