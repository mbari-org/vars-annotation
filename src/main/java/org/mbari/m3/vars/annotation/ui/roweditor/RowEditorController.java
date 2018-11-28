package org.mbari.m3.vars.annotation.ui.roweditor;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.commands.Command;
import org.mbari.m3.vars.annotation.commands.CreateAssociationsCmd;
import org.mbari.m3.vars.annotation.commands.DeleteAssociationsCmd;
import org.mbari.m3.vars.annotation.commands.UpdateAssociationCmd;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Association;

import java.util.*;

/**
 * @author Brian Schlining
 * @since 2017-06-30T07:57:00
 */
public class RowEditorController {
    private AssociationEditorPaneController associationController;
    private AnnotationEditorPaneController rowController;
    private Pane root;
    private volatile Annotation annotation;
    private final UIToolBox toolBox = Initializer.getToolBox();

    public RowEditorController() {
        rowController = AnnotationEditorPaneController.newInstance();
        associationController = AssociationEditorPaneController.newInstance();
        initialize();
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
        Platform.runLater(() -> {
            rowController.setAnnotation(annotation);
            BorderPane rowPane = rowController.getRoot();
            GridPane associationPane = associationController.getRoot();
            ObservableList<Node> children = this.root.getChildren();
            children.remove(associationPane);
            if (!children.contains(rowPane)) {
                children.add(rowPane);
            }
        });
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
            associationController.setTarget(annotation, null);
            this.root.getChildren().remove(rowPane);
            this.root.getChildren().add(associationPane);
            associationController.requestFocus();
        });

        rowController.getEditButton().setOnAction(v -> {
            rowController.getSelectedAssociations()
                    .stream()
                    .findFirst()
                    .ifPresent(ass -> {
                        associationController.setTarget(annotation, ass);
                        this.root.getChildren().remove(rowPane);
                        this.root.getChildren().add(associationPane);
                        associationController.requestFocus();
                    });
        });

        rowController.getRemoveButton().setOnAction(v -> {
            ObservableList<Association> selectedAssociations = rowController.getSelectedAssociations();
            Map<Association, UUID> map = new HashMap<>();
            selectedAssociations.forEach(a -> map.put(a, annotation.getObservationUuid()));
            if (selectedAssociations.size() > 0) {
                Command cmd = new DeleteAssociationsCmd(map);
                toolBox.getEventBus()
                        .send(cmd);
            }
        });

        associationController.getAddButton().setOnAction(v -> doAction());
        associationController.getLinkValueTextField().setOnAction(v -> doAction());

        associationController.getCancelButton().setOnAction(v -> {
            this.root.getChildren().remove(associationPane);
            this.root.getChildren().add(rowPane);
            rowController.requestFocus();
        });
    }

    public Pane getRoot() {
        return root;
    }

    protected void doAction() {
        BorderPane rowPane = rowController.getRoot();
        GridPane associationPane = associationController.getRoot();
        Association selectedAssociation = associationController.getSelectedAssociation();
        Optional<Association> opt = associationController.getCustomAssociation();
        if (opt.isPresent() && annotation != null) {
            Association customAssociation = opt.get();
            Command cmd;
            if (selectedAssociation == null) {
                // Create new association
                cmd = new CreateAssociationsCmd(customAssociation, Arrays.asList(annotation));
            }
            else {
                // Update existing association
                Association a = new Association(selectedAssociation.getUuid(), customAssociation);
                cmd = new UpdateAssociationCmd(annotation.getObservationUuid(), selectedAssociation, a);
            }
            toolBox.getEventBus().send(cmd);
            this.root.getChildren().remove(associationPane);
            this.root.getChildren().add(rowPane);
        }
    }
}
