package org.mbari.vars.ui.javafx.roweditor;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import org.mbari.vars.ui.Initializer;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.commands.Command;
import org.mbari.vars.ui.commands.CreateAssociationsCmd;
import org.mbari.vars.ui.commands.DeleteAssociationsCmd;
import org.mbari.vars.ui.commands.UpdateAssociationCmd;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
//    private final Logger log = LoggerFactory.getLogger(getClass());

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
