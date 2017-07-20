package org.mbari.m3.vars.annotation.ui.roweditor;

import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.util.Pair;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Association;

/**
 * @author Brian Schlining
 * @since 2017-06-30T07:57:00
 */
public class RowEditorController {
    private AssociationEditorPaneController associationController;
    private RowEditorPaneController rowController;
    private Pane root;
    private volatile Annotation annotation;

    public RowEditorController() {
        rowController = RowEditorPaneController.newInstance();
        associationController = AssociationEditorPaneController.newInstance();
        initialize();
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
        Platform.runLater(() -> rowController.setAnnotation(annotation));
    }

    private void initialize() {
        root = new Pane(rowController.getRoot());
        // TODO wire up panes so that they switch context as needed
        rowController.getAddButton().setOnAction(v -> {
            associationController.setTarget(annotation, Association.NIL);
            root.getChildren().remove(rowController.getRoot());
            root.getChildren().add(associationController.getRoot());
        });

        rowController.getEditButton().setOnAction(v -> {
            rowController.setAnnotation(annotation);
            root.getChildren().remove(rowController.getRoot());
            root.getChildren().add(associationController.getRoot());
        });

        rowController.getRemoveButton().setOnAction(v -> {
            // TODO handle delete
        });

        associationController.getAddButton().setOnAction(v -> {
            // TODO
        });
    }

    public Pane getRoot() {
        return root;
    }
}
