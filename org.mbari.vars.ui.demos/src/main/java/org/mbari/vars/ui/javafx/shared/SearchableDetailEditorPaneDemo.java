package org.mbari.vars.ui.javafx.shared;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.mbari.vars.ui.Initializer;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.services.ConceptService;

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
