package org.mbari.m3.vars.annotation.ui;

import com.anchorage.docks.node.DockNode;
import com.anchorage.docks.stations.DockStation;
import com.anchorage.system.AnchorageSystem;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.ui.annotable.AnnotationTableController;
import org.mbari.m3.vars.annotation.ui.cbpanel.ConceptButtonPanesController;
import org.mbari.m3.vars.annotation.ui.concepttree.SearchTreePaneController;

/**
 * @author Brian Schlining
 * @since 2017-07-26T14:37:00
 */
public class AppPaneController {

    private BorderPane root;
    private DockStation dockStation;
    private AnnotationTableController annotationTableController;
    private ToolBar toolBar;
    private final UIToolBox toolBox;

    public AppPaneController(UIToolBox toolBox) {
        this.toolBox = toolBox;
        annotationTableController = new AnnotationTableController(toolBox);
    }

    public BorderPane getRoot() {
        if (root == null) {
            root = new BorderPane(getDockStation());
            root.setTop(getToolBar());
        }
        return root;
    }

    public DockStation getDockStation() {
        if (dockStation == null) {
            dockStation = AnchorageSystem.createStation();
            DockNode annotationNode = AnchorageSystem.createDock("Annotations", annotationTableController.getTableView());
            annotationNode.closeableProperty().set(false);
            annotationNode.dock(dockStation, DockNode.DockPosition.CENTER);

            SearchTreePaneController treeController = new SearchTreePaneController(toolBox.getServices().getConceptService(),
                    toolBox.getI18nBundle());
            DockNode treeNode = AnchorageSystem.createDock("Knowledgebase", treeController.getRoot());
            treeNode.closeableProperty().set(false);
            treeNode.setPrefSize(400, 400);
            treeNode.dock(dockStation, DockNode.DockPosition.RIGHT);

            ConceptButtonPanesController panesController = new ConceptButtonPanesController(toolBox);
            Pane pane = panesController.getRoot();
            pane.setPrefSize(800, 250);
            DockNode cbNode = AnchorageSystem.createDock("Quick Buttons", pane);
            cbNode.closeableProperty().set(false);
            cbNode.maximizableProperty().set(false);
            cbNode.dock(dockStation, DockNode.DockPosition.BOTTOM);


        }
        return dockStation;
    }

    public ToolBar getToolBar() {
        if (toolBar == null) {
            toolBar = new ToolBar(new Button("Open"),
                    new Button("Quit"));
        }
        return toolBar;
    }
}
