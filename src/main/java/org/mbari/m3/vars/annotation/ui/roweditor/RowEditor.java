package org.mbari.m3.vars.annotation.ui.roweditor;

import javafx.scene.layout.Pane;
import javafx.util.Pair;

/**
 * @author Brian Schlining
 * @since 2017-06-30T07:57:00
 */
public class RowEditor {
    private AssociationEditorPaneController associationController;
    private RowEditorPaneController rowController;
    private Pane root;

    public RowEditor() {
        rowController = RowEditorPaneController.newInstance();
        associationController = AssociationEditorPaneController.newInstance();
        initialize();
    }

    private void initialize() {
        root = new Pane(rowController.getRoot());
        // TODO wire up panes so that they switch context as needed
        rowController.getAddButton().setOnAction(v -> {
            root.getChildren().remove(rowController.getRoot());
            root.getChildren().add(rowController.getRoot());
            // TODO wire up jthings after panel is shown
        });

        rowController.getEditButton().setOnAction(v -> {
            root.getChildren().remove(rowController.getRoot());
            root.getChildren().add(rowController.getRoot());
            // TODO wire up jthings after panel is shown
        });

        rowController.getRemoveButton().setOnAction(v -> {
            // TODO handle delete
        });

        associationController.getAddButton().setOnAction(v -> {
            // TODO
        });
    }
}
