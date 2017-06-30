package org.mbari.m3.vars.annotation.ui.roweditor;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import de.jensd.fx.glyphs.GlyphsFactory;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.commands.SelectedAnnotations;
import org.mbari.m3.vars.annotation.model.Annotation;

public class AssociationEditorPaneController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private GridPane root;

    @FXML
    private Label searchLabel;

    @FXML
    private Label linkValueLabel;

    @FXML
    private Label toConceptLabel;

    @FXML
    private JFXButton addButton;

    @FXML
    private JFXButton cancelButton;

    @FXML
    private JFXComboBox<?> associationComboBox;

    @FXML
    private JFXTextField searchTextField;

    @FXML
    private JFXTextField linkNameTextField;

    @FXML
    private JFXComboBox<?> toConceptComboBox;

    @FXML
    private JFXTextField linkValueTextField;

    private final UIToolBox toolBox = Initializer.getToolBox();
    private final EventBus eventBus = toolBox.getEventBus();
    private volatile Annotation annotation;

    @FXML
    void onAdd(ActionEvent event) {

    }

    @FXML
    void onCancel(ActionEvent event) {

    }

    @FXML
    void initialize() {
        GlyphsFactory gf = MaterialIconFactory.get();
        Text addIcon = gf.createIcon(MaterialIcon.ADD);
        addButton.setText(null);
        addButton.setGraphic(addIcon);
        Text cancelIcon = gf.createIcon(MaterialIcon.CANCEL);
        cancelButton.setText(null);
        cancelButton.setGraphic(cancelIcon);

        eventBus.toObserverable()
                .ofType(SelectedAnnotations.class)
                .subscribe(sa -> {
                    Annotation a0 = sa.getAnnotations().size() == 1 ? sa.getAnnotations().get(0) : null;
                    setAnnotation(a0);
                });
    }

    private void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }

    public static Pair<Pane, AssociationEditorPaneController> newInstance() {

        final ResourceBundle bundle = Initializer.getToolBox().getI18nBundle();
        FXMLLoader loader = new FXMLLoader(AssociationEditorPaneController.class
                .getResource("/fxml/AssociationEditorPane.fxml"), bundle);
        try {
            Pane root = loader.load();
            AssociationEditorPaneController controller = loader.getController();
            return new Pair<>(root, controller);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load AssociationEditorPane from FXML", e);
        }

    }
}
