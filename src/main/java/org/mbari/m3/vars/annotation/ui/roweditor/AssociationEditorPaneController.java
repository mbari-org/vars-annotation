package org.mbari.m3.vars.annotation.ui.roweditor;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import de.jensd.fx.glyphs.GlyphsFactory;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import io.reactivex.rxjavafx.observables.JavaFxObservable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.*;
import org.mbari.m3.vars.annotation.services.ConceptService;
import org.mbari.m3.vars.annotation.ui.shared.FilteredComboBoxDecorator;
import org.mbari.m3.vars.annotation.ui.shared.HierarchicalConceptComboBoxDecorator;
import org.mbari.m3.vars.annotation.util.FXMLUtils;
import org.mbari.m3.vars.annotation.util.ListUtils;

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
    private static final ConceptAssociationTemplate nil = ConceptAssociationTemplate.NIL;


    @FXML
    void onAdd(ActionEvent event) {

    }

    @FXML
    void onCancel(ActionEvent event) {

    }

    public GridPane getRoot() {
        return root;
    }

    public Button getAddButton() {
        return addButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public TextField getLinkValueTextField() {
        return linkValueTextField;
    }

    @FXML
    void initialize() {
        JavaFxObservable.valuesOf(root.widthProperty())
                .subscribe(n -> associationComboBox.setPrefWidth(n.doubleValue()));

        GlyphsFactory gf = MaterialIconFactory.get();
        Text addIcon = gf.createIcon(MaterialIcon.ADD, "30px");
        addButton.setText(null);
        addButton.setGraphic(addIcon);
        //addButton.defaultButtonProperty().bind(addButton.focusedProperty()); // Enter triggers button
        Text cancelIcon = gf.createIcon(MaterialIcon.CANCEL, "30px");
        cancelButton.setText(null);
        cancelButton.setGraphic(cancelIcon);
        //cancelButton.defaultButtonProperty().bind(cancelButton.focusedProperty()); // Enter triggers button
        linkNameTextField.setDisable(true);

        // Add filtering of toConcepts
        new FilteredComboBoxDecorator<>(toConceptComboBox, FilteredComboBoxDecorator.STARTSWITH_IGNORE_SPACES);

        // Add decorator to populate combobox with all children of given concept
        toConceptComboBoxDecorator = new HierarchicalConceptComboBoxDecorator(toConceptComboBox,
                toolBox.getServices().getConceptService());

        // Set values in fields when an association is selected
        associationComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldv, newv) -> {
                    if (newv == null) {
                        clear();
                    }
                    else {
                        linkNameTextField.setText(newv.getLinkName());
                        linkValueTextField.setText(newv.getLinkValue());
                        toConceptComboBoxDecorator.setConcept(newv.getToConcept());
                    }
                });

        // Trigger search when enter is pressed in search field
        searchTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                searchTemplates(searchTextField.getText());
            }
        });
    }

    public void clear() {
        searchTextField.setText(null);
        linkNameTextField.setText(null);
        linkValueTextField.setText(null);
        toConceptComboBox.setItems(FXCollections.observableArrayList());
    }

    /**
     *
     * @return A new association built from
     */
    public Optional<Association> getCustomAssociation() {
        String linkName = linkNameTextField.getText();
        String toConcept = toConceptComboBox.getValue();
        String linkValue = linkValueTextField.getText();
        if (linkName != null &&
                !linkName.equals(Association.VALUE_NIL) &&
                toConcept != null &&
                linkValue != null) {
            return Optional.of(new Association(linkName, toConcept, linkValue));
        }
        else {
            return Optional.empty();
        }
    }

    private void searchTemplates(String search) {
        List<ConceptAssociationTemplate> templates = associationComboBox.getItems();
        int startIdx = associationComboBox.getSelectionModel().getSelectedIndex() + 1;
        ListUtils.search(search, templates, startIdx, ConceptAssociationTemplate::toString)
                .ifPresent(cat -> associationComboBox.getSelectionModel().select(cat));
    }

    public synchronized void setTarget(Annotation annotation, Association association) {
        this.annotation = annotation;
        this.selectedAssociation = association;

        ConceptAssociationTemplate defaultAssociation = null;

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
            ObservableList<String> items = FXCollections.observableArrayList();
            items.add(concept);
            toConceptComboBox.setItems(items);

        }
        else {
            // TODO look up the link templates. Find a match linkvalue and set it's toconcept, then select the cat's toConcept
//            toolBox.getServices()
//                    .getConceptService()
//                    .findTemplates(name, cat.getLinkName())
//                    .thenAccept(cats -> {
//
//                    });
            toConceptComboBoxDecorator.setConcept(concept);
            toConceptComboBox.getSelectionModel().select(cat.getToConcept());
        }
    }

    public void requestFocus() {
        searchTextField.requestFocus();
    }

    /**
     *
     * @return The association that was original set in the editor. May be null if you're
     *      creating a new one.
     */
    public Association getSelectedAssociation() {
        return selectedAssociation;
    }

    public static AssociationEditorPaneController newInstance() {

        return FXMLUtils.newInstance(AssociationEditorPaneController.class,
                "/fxml/AssociationEditorPane.fxml");
    }
}
