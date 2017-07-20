package org.mbari.m3.vars.annotation.ui.roweditor;

import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
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
        BorderPane rowPane = rowController.getRoot();
        GridPane associationPane = associationController.getRoot();

        this.root = new Pane(rowPane);
        rowPane.prefWidthProperty().bind(root.widthProperty());
        rowPane.prefHeightProperty().bind(root.heightProperty());
        associationPane.prefWidthProperty().bind(root.widthProperty());
        associationPane.prefHeightProperty().bind(root.heightProperty());

        rowController.getAddButton().setOnAction(v -> {
            associationController.setTarget(annotation, Association.NIL);
            this.root.getChildren().remove(rowPane);
            this.root.getChildren().add(associationPane);
        });

        rowController.getEditButton().setOnAction(v -> {
            rowController.setAnnotation(annotation);
            this.root.getChildren().remove(rowPane);
            this.root.getChildren().add(associationPane);
        });

        rowController.getRemoveButton().setOnAction(v -> {
            // TODO handle delete
        });

        associationController.getAddButton().setOnAction(v -> {

            // TODO
        });

        associationController.getCancelButton().setOnAction(v -> {
            this.root.getChildren().remove(associationPane);
            this.root.getChildren().add(rowPane);
        });
    }

    public Pane getRoot() {
        return root;
    }
}
