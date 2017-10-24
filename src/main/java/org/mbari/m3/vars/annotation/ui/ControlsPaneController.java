package org.mbari.m3.vars.annotation.ui;

import com.jfoenix.controls.JFXButton;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.events.AnnotationsSelectedEvent;
import org.mbari.m3.vars.annotation.mediaplayers.sharktopoda.SharktoptodaControlPane;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.ui.abpanel.AssocButtonPaneController;
import org.mbari.m3.vars.annotation.ui.buttons.*;
import org.mbari.m3.vars.annotation.ui.roweditor.RowEditorController;

import java.util.Collection;
import java.util.Optional;
import java.util.prefs.Preferences;

/**
 * @author Brian Schlining
 * @since 2017-09-14T11:28:00
 */
public class ControlsPaneController {

    private SplitPane root;
    private SharktoptodaControlPane sharkPane;
    private RowEditorController rowEditorController;
    private AssocButtonPaneController assocBtnPane;
    private final UIToolBox toolBox;
    private FlowPane buttonPane;
    private VBox rightPane;
    private static final String splitPaneKey = "split-pane";

    public ControlsPaneController(UIToolBox toolBox) {
        this.toolBox = toolBox;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            saveDividerPositions(splitPaneKey, getRoot());
        }));
    }

    public SplitPane getRoot() {
        if (root == null) {
            if (getAssocBtnPane() == null) {
                root = new SplitPane(getRowEditorController().getRoot(), getRightPane());
            }
            else {
                root = new SplitPane(getRowEditorController().getRoot(),
                        getRightPane(),
                        getAssocBtnPane().getPane());
            }
            loadDividerPositions(splitPaneKey, root);
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

            Button dupBtn = new JFXButton();
            new DuplicateAnnotationBC(dupBtn, toolBox);

            Button copyBtn = new JFXButton();
            new CopyAnnotationBC(copyBtn, toolBox);

            Button framegrabBtn = new JFXButton();
            new FramecaptureBC(framegrabBtn, toolBox);

            Button sampleBtn = new JFXButton();
            new SampleBC(sampleBtn, toolBox);

            Button commentBtn = new JFXButton();
            new CommentBC(commentBtn, toolBox);

            Button newRefBtn = new JFXButton();
            new NewReferenceNumberBC(newRefBtn, toolBox);

            Button oldRefBtn = new JFXButton();
            new OldReferenceNumberBC(oldRefBtn, toolBox);

            Button uponBtn = new JFXButton();
            new UponBC(uponBtn, toolBox);

            Button durationBtn = new JFXButton();
            new SetDurationBC(durationBtn, toolBox);

//            Button denseBtn = new JFXButton();
//            new TempDenseBC(denseBtn, toolBox);
//
//            Button ninesBtn = new JFXButton();
//            new TempPopulationNinesBC(ninesBtn, toolBox);

            buttonPane.getChildren().addAll(newBtn, dupBtn, copyBtn, framegrabBtn,
                    sampleBtn, newRefBtn, oldRefBtn, uponBtn,  commentBtn, durationBtn,
                    deleteBtn);

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

    private AssocButtonPaneController getAssocBtnPane() {
        if (assocBtnPane == null) {
            assocBtnPane = new AssocButtonPaneController(toolBox);
        }
        return assocBtnPane;
    }

    private VBox getRightPane() {
        if (rightPane == null) {
            rightPane = new VBox(getSharkPane(), getButtonPane());
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
            MaterialIconFactory iconFactory = MaterialIconFactory.get();
            Text icon = iconFactory.createIcon(MaterialIcon.CREATE, "30px");
            initializeButton(tooltip, icon);

            // TODO show dialog for adding and annotation to the pane
        }
    }




}
