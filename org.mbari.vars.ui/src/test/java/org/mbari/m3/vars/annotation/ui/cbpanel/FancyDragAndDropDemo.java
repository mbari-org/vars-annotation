package org.mbari.m3.vars.annotation.ui.cbpanel;

import javafx.application.Application;
import javafx.stage.Stage;
import org.mbari.m3.vars.annotation.Initializer;

import java.util.List;

/**
 * @author Brian Schlining
 * @since 2017-06-15T09:02:00
 */
public class FancyDragAndDropDemo extends Application {

    public static void main(String[] args) throws Exception {

        List<String> concepts = Initializer.getToolBox()
                .getConfig()
                .getStringList("app.annotation.details.cache");

        Thread.sleep(2000);
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
//        DockStation station = AnchorageSystem.createStation();
//
//        UIToolBox toolBox = DemoConstants.getToolBox();
//        CompletableFuture<List<User>> allUsers = toolBox.getServices().getUserService().findAllUsers();
//
//        AnnotationTableController annoController = new AnnotationTableController(toolBox);
//        DockNode annotationNode = AnchorageSystem.createDock("Annotations", annoController.getTableView());
//        annotationNode.setCloseRequestHandler(() -> false);
//        annotationNode.dock(station, DockNode.DockPosition.CENTER);
//
//        SearchTreePaneController treeController = new SearchTreePaneController(toolBox.getServices().getConceptService(),
//                toolBox.getI18nBundle());
//        DockNode treeNode = AnchorageSystem.createDock("Knowledgebase", treeController.getRoot());
//        treeNode.setCloseRequestHandler(() -> false);
//        treeNode.setPrefSize(400, 400);
//        treeNode.dock(station, DockNode.DockPosition.RIGHT);
//
//        ConceptButtonPanesController panesController = new ConceptButtonPanesController(toolBox);
//        Pane pane = panesController.getRoot();
//        pane.setPrefSize(800, 250);
//        DockNode cbNode = AnchorageSystem.createDock("Quick Buttons", pane);
//        cbNode.setCloseRequestHandler(() -> false);
//        cbNode.dock(station, DockNode.DockPosition.BOTTOM);
//
//        try {
//            List<User> users = allUsers.get(7000, TimeUnit.MILLISECONDS);
//            Optional<User> brian = users.stream()
//                    .filter(u -> u.getUsername().equalsIgnoreCase("brian"))
//                    .findFirst();
//            toolBox.getData().setUser(brian.get());
//        }
//        catch (Exception e) {
//            // Do nothing
//        }
//
//        Scene scene = new Scene(station);
//
//        scene.getStylesheets().addAll("/css/dark.css",
//                "/css/common.css",
//                "/css/annotable.css",
//                "/css/concepttree.css",
//                "/css/cbpanel.css");
//
//        try {
//            List<User> users = allUsers.get(7000, TimeUnit.MILLISECONDS);
//            Optional<User> brian = users.stream()
//                    .filter(u -> u.getUsername().equalsIgnoreCase("brian"))
//                    .findFirst();
//            toolBox.getData().setUser(brian.get());
//        }
//        catch (Exception e) {
//            // Do nothing
//        }
//
//        primaryStage.setScene(scene);
//        primaryStage.setOnCloseRequest(e -> {
//            Platform.exit();
//            System.exit(0);
//        });
//        primaryStage.show();

    }
}
