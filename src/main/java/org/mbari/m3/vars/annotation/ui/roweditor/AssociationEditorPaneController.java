package org.mbari.m3.vars.annotation.ui.roweditor;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import de.jensd.fx.glyphs.GlyphsFactory;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.*;
import org.mbari.m3.vars.annotation.services.ConceptService;
import org.mbari.m3.vars.annotation.ui.shared.FilteredComboBoxDecorator;
import org.mbari.m3.vars.annotation.ui.shared.HierarchicalConceptComboBoxDecorator;

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

    private HierarchicalConceptComboBoxDecorator toConceptComboBoxDecorator;
    private final UIToolBox toolBox = Initializer.getToolBox();
    private final EventBus eventBus = toolBox.getEventBus();
    private volatile Annotation annotation;
    private volatile Association selectedAssociation;
    private static final ConceptAssociationTemplate nil = new ConceptAssociationTemplate(Association.VALUE_NIL, Association.VALUE_NIL, Association.VALUE_NIL);


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
        new FilteredComboBoxDecorator<>(toConceptComboBox, FilteredComboBoxDecorator.CONTAINS_CHARS_IN_ORDER);
        toConceptComboBoxDecorator = new HierarchicalConceptComboBoxDecorator(toConceptComboBox,
                toolBox.getServices().getConceptService());

    }

    public synchronized void setTarget(Annotation annotation, Association association) {
        this.annotation = annotation;
        this.selectedAssociation = association;

        ConceptAssociationTemplate defaultAssociation = null;

        // TODO handle null annotation

        /*
         * ---- Step 1:
         * If an association is set we're editing it. If not we're adding it.
         *
         * Here we figure out what the intial link and conceptName should be.
         */
        if (association != null) {
            defaultAssociation = new ConceptAssociationTemplate(association.getLinkName(),
                    association.getToConcept(), association.getLinkValue());
            addButton.setTooltip(new Tooltip("Accept Edits"));
        }
        else {
            defaultAssociation = nil;
            addButton.setTooltip(new Tooltip("Add Association"));
        }

        /*
         * ---- Step 2:
         * Update the available templates in the UI
         */
        associationComboBox.getItems().clear();
        associationComboBox.getItems().add(nil);
        if (annotation != null) {
            ConceptService cs = toolBox.getServices().getConceptService();
            final ConceptAssociationTemplate cat = defaultAssociation; // final to satisfy lambda
            cs.findTemplates(annotation.getConcept())
                    .thenApply(templates -> {
                        Platform.runLater(() -> {
                            associationComboBox.getItems().clear();
                            associationComboBox.getItems().addAll(templates);
                            if (!associationComboBox.getItems().contains(cat)) {
                                associationComboBox.getItems().add(cat);
                            }
                            associationComboBox.getSelectionModel().select(cat);
                            setAssociation(annotation.getConcept(), cat);
                        });
                        return null;
                    });
        }
    }

    private void setAssociation(String name, ConceptAssociationTemplate cat) {
        linkNameTextField.setText(cat.getLinkName());
        linkValueTextField.setText(cat.getLinkValue());
        String concept = cat.getToConcept();
        if (concept.equals(Association.VALUE_NIL) || concept.equals(Association.VALUE_SELF)) {
            toConceptComboBox.getItems().clear();
            toConceptComboBox.getItems().add(concept);
        }
        else {
            // TODO look up the link templates. Find a match linkvalue and set it's toconcept, then select the cat's toConcept
            toConceptComboBoxDecorator.setConcept(concept);
        }
    }





    private void setToConcepts(List<String> concepts, String selectedConcept) {
        Platform.runLater(() -> {
            toConceptComboBox.getItems().addAll(concepts);
            toConceptComboBox.getSelectionModel()
                    .select(selectedConcept);
        });
    }

    private CompletableFuture<List<String>> findChildConcepts(String concept) {
        ConceptService cs = toolBox.getServices().getConceptService();
        return cs.findConcept(concept)
                .thenApply(opt -> {
                    List<String> childConcepts = opt.map(Concept::flatten)
                            .orElseGet(() -> {
                                List<String> n = new ArrayList<>();
                                n.add(Association.VALUE_NIL);
                                return n;
                            });
                    return childConcepts;
                });
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
