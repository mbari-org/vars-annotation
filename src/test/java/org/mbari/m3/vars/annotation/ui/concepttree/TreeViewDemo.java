package org.mbari.m3.vars.annotation.ui.concepttree;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import org.mbari.m3.vars.annotation.model.Concept;
import org.mbari.m3.vars.annotation.services.CachedConceptService;
import org.mbari.m3.vars.annotation.services.ConceptService;
import org.mbari.m3.vars.annotation.services.varskbserver.v1.KBConceptService;
import org.mbari.m3.vars.annotation.services.varskbserver.v1.KBWebServiceFactory;

/**
 * @author Brian Schlining
 * @since 2017-05-15T18:20:00
 */
public class TreeViewDemo extends Application {

    private ConceptService conceptService = new CachedConceptService(
            new KBConceptService(new KBWebServiceFactory("http://m3.shore.mbari.org/kb/v1/")));

//    private ConceptService conceptService = new KBConceptService("http://m3.shore.mbari.org/kb/v1/");
    private TreeViewController controller = new TreeViewController(conceptService);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Platform.setImplicitExit(true);
        TreeView<Concept> treeView = controller.getTreeView();
        Scene scene = new Scene(treeView, 800, 800);
        primaryStage.setScene(scene);

        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();

    }
}
