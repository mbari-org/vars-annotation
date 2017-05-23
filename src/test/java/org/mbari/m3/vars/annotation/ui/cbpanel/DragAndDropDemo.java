package org.mbari.m3.vars.annotation.ui.cbpanel;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.services.CachedConceptService;
import org.mbari.m3.vars.annotation.services.varskbserver.v1.KBConceptService;
import org.mbari.m3.vars.annotation.ui.concepttree.SearchTreePaneFactory;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

/**
 * @author Brian Schlining
 * @since 2017-05-17T16:03:00
 */
public class DragAndDropDemo extends Application {

    private static CachedConceptService conceptService = new CachedConceptService(
            new KBConceptService("http://m3.shore.mbari.org/kb/v1/"));

    private static EventBus eventBus = new EventBus();

    private static ResourceBundle uiBundle = ResourceBundle.getBundle("UIBundle",
            Locale.getDefault());

    public static void main(String[] args) throws InterruptedException {
        CompletableFuture<Void> f = conceptService.prefetch();
        while (!f.isDone()) {
            Thread.sleep(20);
        }
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Platform.setImplicitExit(true);
        SearchTreePaneFactory paneBuilder = new SearchTreePaneFactory(conceptService, uiBundle);
        BorderPane node = paneBuilder.build();
        FlowPane pane = new FlowPane();
        pane.setPrefSize(800, 250);
        PaneDecorator paneDecorator = new PaneDecorator(conceptService, eventBus, uiBundle);
        paneDecorator.decorate(pane);
        node.setBottom(pane);
        Scene scene = new Scene(node, 800, 800);
        scene.getStylesheets().add("/application.css");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
    }

}
