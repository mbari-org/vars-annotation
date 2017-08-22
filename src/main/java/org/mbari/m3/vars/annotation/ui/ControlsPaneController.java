package org.mbari.m3.vars.annotation.ui;

import com.jfoenix.controls.JFXButton;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.events.AnnotationsSelectedEvent;
import org.mbari.m3.vars.annotation.mediaplayers.sharktopoda.SharktoptodaControlPane;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.ui.buttons.DeleteSelectedAnnotationsBC;
import org.mbari.m3.vars.annotation.ui.buttons.NewAnnotationBC;
import org.mbari.m3.vars.annotation.ui.roweditor.RowEditorController;

import java.util.Collection;

/**
 * @author Brian Schlining
 * @since 2017-08-15T15:31:00
 */
public class ControlsPaneController {

    private BorderPane root;
    private SharktoptodaControlPane sharkPane;
    private RowEditorController rowEditorController;
    private final UIToolBox toolBox;
    private FlowPane buttonPane;

    public ControlsPaneController(UIToolBox toolBox) {
        this.toolBox = toolBox;
    }

    public BorderPane getRoot() {
        if (root == null) {
            root = new BorderPane();
            root.setRight(getSharkPane());
            root.setCenter(getButtonPane());
            root.setLeft(getRowEditorController().getRoot());
        }
        return root;
    }

    public SharktoptodaControlPane getSharkPane() {
        if (sharkPane == null) {
            sharkPane = new SharktoptodaControlPane(toolBox);
            toolBox.mediaPlayerProperty()
                    .addListener((obs, oldv, newv) -> sharkPane.setMediaPlayer(newv));
        }
        return sharkPane;
    }


    public FlowPane getButtonPane() {
        if (buttonPane == null) {
            buttonPane = new FlowPane();

            Button deleteBtn = new JFXButton();
            new DeleteSelectedAnnotationsBC(deleteBtn, toolBox);

            Button newBtn = new JFXButton();
            new NewAnnotationBC(newBtn, toolBox);

            buttonPane.getChildren().addAll(newBtn, deleteBtn);

        }
        return buttonPane;
    }

    public RowEditorController getRowEditorController() {
        if (rowEditorController == null) {
            rowEditorController = new RowEditorController();
            rowEditorController.getRoot().setPrefSize(700, 300);
            toolBox.getEventBus()
                    .toObserverable()
                    .ofType(AnnotationsSelectedEvent.class)
                    .subscribe(e -> {
                        Collection<Annotation> annotations = e.get();
                        if (annotations.size() == 1) {
                            rowEditorController.setAnnotation(annotations.iterator().next());
                        }
                        else {
                            rowEditorController.setAnnotation(null);
                        }
                    });
        }
        return rowEditorController;
    }
}
