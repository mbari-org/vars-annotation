package org.mbari.vars.ui.javafx;

import com.jfoenix.controls.JFXButton;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.events.AnnotationsSelectedEvent;
import org.mbari.vars.ui.events.MediaControlsChangedEvent;
import org.mbari.vars.ui.mediaplayers.MediaControls;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.ui.services.CachedReferenceNumberDecorator;
import org.mbari.vars.ui.javafx.abpanel.AssocButtonPanesController;
import org.mbari.vars.ui.javafx.buttons.*;
import org.mbari.vars.ui.javafx.roweditor.RowEditorController;

import java.util.Collection;
import java.util.prefs.Preferences;

/**
 * @author Brian Schlining
 * @since 2017-09-14T11:28:00
 */
public class ControlsPaneController {

    private SplitPane root;
    private Pane emptyControlsPane;
    private RowEditorController rowEditorController;
    private AssocButtonPanesController assocBtnPane;
    private final UIToolBox toolBox;
    private FlowPane buttonPane;
    private VBox rightPane;
    private static final String splitPaneKey = "split-pane";

    public ControlsPaneController(UIToolBox toolBox) {
        this.toolBox = toolBox;

        toolBox.getEventBus()
                .toObserverable()
                .ofType(MediaControlsChangedEvent.class)
                .subscribe(e -> updateMediaControlPane(e.get()));

        emptyControlsPane = new Pane();
        emptyControlsPane.setPrefSize(440, 80);
        emptyControlsPane.setMaxSize(440, 80);
        emptyControlsPane.setMinSize(440, 80);

        Runtime.getRuntime()
                .addShutdownHook(new Thread(() -> saveDividerPositions(splitPaneKey, getRoot())));
    }

    private void updateMediaControlPane(MediaControls mediaControls) {
        Platform.runLater(() -> {
            Pane pane = emptyControlsPane;
            if (mediaControls != null && mediaControls.getMediaControlPanel() != null) {
                pane = mediaControls.getMediaControlPanel();
            }

            getRightPane().getChildren().remove(0);
            getRightPane().getChildren().add(0, pane);
        });
    }

    public SplitPane getRoot() {
        if (root == null) {
            if (getAssocBtnPane() == null) {
                root = new SplitPane(getRowEditorController().getRoot(), getRightPane());
            }
            else {
                root = new SplitPane(getRowEditorController().getRoot(),
                        getRightPane(),
                        getAssocBtnPane().getRoot());
            }
            loadDividerPositions(splitPaneKey, root);
        }
        return root;
    }



    public FlowPane getButtonPane() {
        if (buttonPane == null) {
            buttonPane = new FlowPane();

            Button deleteBtn = new JFXButton();
            new DeleteSelectedAnnotationsBC(deleteBtn, toolBox);

            Button newBtn = new JFXButton();
            new NewAnnotationBC(newBtn, toolBox);

            Button dupBtn = new JFXButton();
            new DuplicateAnnotationBC(dupBtn, toolBox);

            Button copyBtn = new JFXButton();
            new CopyAnnotationBC(copyBtn, toolBox);

            Button framegrabBtn = new JFXButton();
            new FramecaptureBC(framegrabBtn, toolBox);

            Button detachBtn = new JFXButton();
            new DetachFramegrabsBC(detachBtn, toolBox);

            Button sampleBtn = new JFXButton();
            new SampleBC(sampleBtn, toolBox);

            Button commentBtn = new JFXButton();
            new CommentBC(commentBtn, toolBox);

            CachedReferenceNumberDecorator decorator = new CachedReferenceNumberDecorator(toolBox);

            Button newRefBtn = new JFXButton();
            new NewReferenceNumberBC(newRefBtn, toolBox, decorator);

            Button oldRefBtn = new JFXButton();
            new OldReferenceNumberBC(oldRefBtn, toolBox, decorator);

            Button uponBtn = new JFXButton();
            new UponBC(uponBtn, toolBox);

            Button pqBtn = new JFXButton();
            new PopulationQuantityBC(pqBtn, toolBox);

            Button durationBtn = new JFXButton();
            new SetDurationBC(durationBtn, toolBox);

            Button deleteDurationBtn = new JFXButton();
            new DeleteDurationBC(deleteDurationBtn, toolBox);

            Button mlBtn = new JFXButton();
            new MachineLearningBC(mlBtn, toolBox);

            Button bbBtn = new JFXButton();
            new NewBoundingBoxBC(bbBtn, toolBox);

//            Button denseBtn = new JFXButton();
//            new TempDenseBC(denseBtn, toolBox);
//
//            Button ninesBtn = new JFXButton();
//            new TempPopulationNinesBC(ninesBtn, toolBox);

            buttonPane.getChildren().addAll(newBtn, dupBtn, copyBtn, framegrabBtn,
                    detachBtn, mlBtn, bbBtn, sampleBtn, newRefBtn, oldRefBtn, uponBtn, pqBtn,
                    commentBtn, durationBtn, deleteDurationBtn, deleteBtn);

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

    private AssocButtonPanesController getAssocBtnPane() {
        if (assocBtnPane == null) {
            assocBtnPane = new AssocButtonPanesController(toolBox);
        }
        return assocBtnPane;
    }

    private VBox getRightPane() {
        if (rightPane == null) {
            rightPane = new VBox(emptyControlsPane, getButtonPane());
        }
        return rightPane;
    }

    private void saveDividerPositions(String name, SplitPane pane) {
        // Pref path is UserNode/Class/name/0 (or 1 or 2)
        Preferences p0 = Preferences.userNodeForPackage(getClass());
        Preferences p1 = p0.node(name);
        double[] pos = pane.getDividerPositions();
        for (int i = 0; i < pos.length; i++) {
            p1.putDouble(i+ "", pos[i]);
        }
    }

    private void loadDividerPositions(String name, SplitPane pane) {
        Preferences p0 = Preferences.userNodeForPackage(getClass());
        Preferences p1 = p0.node(name);
        double[] positions = pane.getDividerPositions();
        for (int i = 0; i < positions.length; i++) {
            try {
                double v = p1.getDouble(i + "", positions[i]);
                pane.setDividerPosition(i, v);
            }
            catch (Exception e) {
                // TODO log it
            }
        }
    }

    class AddAssociationBC extends AbstractBC {

        public AddAssociationBC(Button button, UIToolBox toolBox) {
            super(button, toolBox);
        }

        @Override
        protected void apply() {

        }

        @Override
        protected void init() {
            String tooltip = toolBox.getI18nBundle().getString("buttons.association");
            Text icon = Icons.CREATE.standardSize();
            initializeButton(tooltip, icon);

            // TODO show dialog for adding and annotation to the pane
        }
    }




}
