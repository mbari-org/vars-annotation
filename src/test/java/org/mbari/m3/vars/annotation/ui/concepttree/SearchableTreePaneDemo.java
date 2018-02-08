package org.mbari.m3.vars.annotation.ui.concepttree;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.services.CachedConceptService;
import org.mbari.m3.vars.annotation.services.ConceptService;
import org.mbari.m3.vars.annotation.services.varskbserver.v1.KBConceptService;
import org.mbari.m3.vars.annotation.services.varskbserver.v1.KBWebServiceFactory;
import org.mbari.m3.vars.annotation.ui.DemoConstants;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

/**
 * @author Brian Schlining
 * @since 2017-05-16T12:48:00
 */
public class SearchableTreePaneDemo extends Application {

    private static ConceptService conceptService = DemoConstants.newConceptService();
    private static ResourceBundle uiBundle = DemoConstants.UI_BUNDLE;

    public static void main(String[] args) throws InterruptedException {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Platform.setImplicitExit(true);
        SearchTreePaneController controller = new SearchTreePaneController(conceptService, uiBundle);
        BorderPane node = controller.getRoot();
        Scene scene = new Scene(node, 800, 800);
        scene.getStylesheets().addAll(Initializer.getToolBox().getStylesheets());
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
    }
}
