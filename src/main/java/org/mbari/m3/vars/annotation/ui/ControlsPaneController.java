package org.mbari.m3.vars.annotation.ui;

import javafx.scene.layout.BorderPane;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.events.AnnotationsSelectedEvent;
import org.mbari.m3.vars.annotation.mediaplayers.sharktopoda.SharktoptodaControlPane;
import org.mbari.m3.vars.annotation.model.Annotation;
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

    public ControlsPaneController(UIToolBox toolBox) {
        this.toolBox = toolBox;
    }

    public BorderPane getRoot() {
        if (root == null) {
            root = new BorderPane();
            root.setRight(getSharkPane());
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
