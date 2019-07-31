package org.mbari.vars.ui.demos.javafx.concepttree;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import org.mbari.vars.ui.Initializer;
import org.mbari.vars.ui.demos.javafx.DemoConstants;
import org.mbari.vars.services.ConceptService;
import org.mbari.vars.services.model.Concept;
import org.mbari.vars.ui.javafx.concepttree.TreeViewController;

/**
 * @author Brian Schlining
 * @since 2017-05-15T18:20:00
 */
public class TreeViewDemo extends Application {

    private ConceptService conceptService = DemoConstants.newConceptService();
    private TreeViewController controller = new TreeViewController(conceptService);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Platform.setImplicitExit(true);
        TreeView<Concept> treeView = controller.getTreeView();
        Scene scene = new Scene(treeView, 800, 800);
        scene.getStylesheets().addAll(Initializer.getToolBox().getStylesheets());
        primaryStage.setScene(scene);

        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();

    }
}
