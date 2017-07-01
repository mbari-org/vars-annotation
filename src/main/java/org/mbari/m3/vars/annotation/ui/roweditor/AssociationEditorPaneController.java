package org.mbari.m3.vars.annotation.ui.roweditor;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import de.jensd.fx.glyphs.GlyphsFactory;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.m3.vars.annotation.model.Concept;
import org.mbari.m3.vars.annotation.model.ConceptAssociationTemplate;
import org.mbari.m3.vars.annotation.services.ConceptService;

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
    private JFXComboBox<ConceptAssociationTemplate> associationComboBox;

    @FXML
    private JFXTextField searchTextField;

    @FXML
    private JFXTextField linkNameTextField;

    @FXML
    private JFXComboBox<String> toConceptComboBox;

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

    public GridPane getRoot() {
        return root;
    }

    public JFXButton getAddButton() {
        return addButton;
    }

    public JFXButton getCancelButton() {
        return cancelButton;
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

    }

    public void setAssociation(Association association) {
        this.annotation = annotation;
        linkNameTextField.setText(association.getLinkName());
        linkValueTextField.setText(association.getLinkValue());
        String toConcept = association.getToConcept() == null ?
                Association.VALUE_NIL : association.getToConcept();
        toConceptComboBox.getItems().clear();
        if (toConcept.equalsIgnoreCase(Association.VALUE_NIL) ||
                toConcept.equalsIgnoreCase(Association.VALUE_SELF)) {
            toConceptComboBox.getItems().add(toConcept);
        }
        else {
            ConceptService cs = toolBox.getServices().getConceptService();
            cs.findTemplates(toConcept)
                    .thenApply(templates -> {
                        // Put templates in combobos
                        Platform.runLater(() -> {
                            associationComboBox.getItems().clear();
                            associationComboBox.getItems().addAll(templates);
                        });
                        // Get the root concept for this link template
                        return templates.stream()
                                .filter(t -> t.getLinkName().equalsIgnoreCase(association.getLinkName()))
                                .findFirst()
                                .map(ConceptAssociationTemplate::getToConcept);
                    })
                    .thenApply(opt -> {
                        opt.ifPresent(c -> {
                            cs.fetchConceptTree(toConcept)
                                    .thenApply(opt0 -> {
                                        List<String> names = opt0.map(Concept::flatten)
                                                .orElseGet(() -> {
                                                    List<String> n = new ArrayList<>();
                                                    n.add(Association.VALUE_NIL);
                                                    return n;
                                                });
                                        Platform.runLater(() -> {
                                            toConceptComboBox.getItems().addAll(names);
                                            toConceptComboBox.getSelectionModel()
                                                    .select(toConcept);
                                        });
                                        return null;
                                    });
                        });
                        return null;
                    });


        }
    }



    public static AssociationEditorPaneController newInstance() {

        final ResourceBundle bundle = Initializer.getToolBox().getI18nBundle();
        FXMLLoader loader = new FXMLLoader(AssociationEditorPaneController.class
                .getResource("/fxml/AssociationEditorPane.fxml"), bundle);
        try {
            loader.load();
            return loader.getController();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load AssociationEditorPane from FXML", e);
        }

    }
}
