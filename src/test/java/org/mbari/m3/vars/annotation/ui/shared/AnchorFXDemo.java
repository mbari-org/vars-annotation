package org.mbari.m3.vars.annotation.ui.shared;

import com.anchorage.docks.node.DockNode;
import com.anchorage.docks.stations.DockStation;
import com.anchorage.system.AnchorageSystem;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.services.CachedConceptService;
import org.mbari.m3.vars.annotation.services.ConceptService;
import org.mbari.m3.vars.annotation.ui.DemoConstants;
import org.mbari.m3.vars.annotation.ui.annotable.AnnotationTableController;
import org.mbari.m3.vars.annotation.ui.cbpanel.DragPaneDecorator;
import org.mbari.m3.vars.annotation.ui.concepttree.SearchTreePaneController;

import java.util.concurrent.CompletableFuture;

/**
 * @author Brian Schlining
 * @since 2017-06-12T15:27:00
 */
public class AnchorFXDemo extends Application {

    private static ConceptService conceptService = DemoConstants.newConceptService();

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        DockStation station = AnchorageSystem.createStation();

        UIToolBox toolBox = DemoConstants.getToolBox();
        AnnotationTableController controller = new AnnotationTableController(toolBox);
        DockNode annotationNode = AnchorageSystem.createDock("Annotations", controller.getTableView());
        annotationNode.dock(station, DockNode.DockPosition.CENTER);

        SearchTreePaneController paneBuilder = new SearchTreePaneController(conceptService, toolBox.getI18nBundle());
        DockNode treeNode = AnchorageSystem.createDock("Knowledgebase", paneBuilder.getRoot());
        treeNode.dock(station, DockNode.DockPosition.RIGHT);

        FlowPane pane = new FlowPane();
        pane.setPrefSize(800, 250);
        DragPaneDecorator dragPaneDecorator = new DragPaneDecorator(conceptService,
                toolBox.getEventBus(),
                toolBox.getI18nBundle());
        dragPaneDecorator.decorate(pane);
        DockNode cbNode = AnchorageSystem.createDock("Quick Buttons", pane);
        cbNode.dock(station, DockNode.DockPosition.BOTTOM);

        Scene scene = new Scene(station);

        scene.getStylesheets().add("/css/common.css");
        scene.getStylesheets().add("/css/annotable.css");

        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }
}
