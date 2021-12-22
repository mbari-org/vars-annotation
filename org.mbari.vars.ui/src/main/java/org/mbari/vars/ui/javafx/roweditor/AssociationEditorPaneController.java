package org.mbari.vars.ui.javafx.roweditor;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

//import io.reactivex.rxjavafx.observables.JavaFxObservable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.mbari.vars.core.EventBus;
import org.mbari.vars.ui.Initializer;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.javafx.Icons;
import org.mbari.vars.services.model.*;
import org.mbari.vars.services.ConceptService;
import org.mbari.vars.ui.javafx.shared.FilteredComboBoxDecorator;
import org.mbari.vars.ui.javafx.shared.HierarchicalConceptComboBoxDecorator;
import org.mbari.vars.ui.util.FXMLUtils;
import org.mbari.vars.core.util.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private Label linkNameLabel;

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
    private ComboBox<String> toConceptComboBox;

    @FXML
    private JFXTextField linkValueTextField;

    private HierarchicalConceptComboBoxDecorator toConceptComboBoxDecorator;
    private final UIToolBox toolBox = Initializer.getToolBox();
    private final EventBus eventBus = toolBox.getEventBus();
    private volatile Annotation annotation;
    private volatile Association selectedAssociation;
    private static final ConceptAssociationTemplate nil = ConceptAssociationTemplate.NIL;
    private final Logger log = LoggerFactory.getLogger(getClass());

    // HACK, I pulled these colors out of annotation.css
    private final Color selectedTextColor = Color.web("#F0544C");
    private final Color defaultTextColor = Color.web("#B3A9A3");



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
        associationComboBox.prefWidthProperty().bind(root.widthProperty());
        toConceptComboBox.focusedProperty().addListener((obs, oldv, newv) -> {
            if (newv) {
                toConceptLabel.setTextFill(selectedTextColor);
            }
            else {
                toConceptLabel.setTextFill(defaultTextColor);
            }
        });

        linkValueTextField.focusedProperty().addListener((obs, oldv, newv) -> {
            if (newv) {
                linkValueLabel.setTextFill(selectedTextColor);
            }
            else {
                linkValueLabel.setTextFill(defaultTextColor);
            }
        });

        searchTextField.focusedProperty().addListener((obs, oldv, newv) -> {
            if (newv) {
                searchLabel.setTextFill(selectedTextColor);
            }
            else {
                searchLabel.setTextFill(defaultTextColor);
            }
        });


        Text addIcon = Icons.ADD.standardSize();
        addButton.setText(null);
        addButton.setGraphic(addIcon);
        //addButton.defaultButtonProperty().bind(addButton.focusedProperty()); // Enter triggers button
        Text cancelIcon = Icons.CANCEL.standardSize();
        cancelButton.setText(null);
        cancelButton.setGraphic(cancelIcon);
        //cancelButton.defaultButtonProperty().bind(cancelButton.focusedProperty()); // Enter triggers button
        linkNameTextField.setDisable(true);

        // Add filtering of toConcepts
        new FilteredComboBoxDecorator<>(toConceptComboBox, FilteredComboBoxDecorator.STARTSWITH_IGNORE_SPACES);

        // Add decorator to populate combobox with all children of given concept
        toConceptComboBoxDecorator = new HierarchicalConceptComboBoxDecorator(toConceptComboBox,
                toolBox);

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
//                        toConceptComboBoxDecorator.setConcept(newv.getToConcept());
                    }
                });



        // Trigger search when enter is pressed in search field
        searchTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                event.consume();
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
                            setAssociation(annotation.getConcept(), cat, templates);
                        });
                        return null;
                    });
        }
    }

    private void setAssociation(String name, ConceptAssociationTemplate cat, List<ConceptAssociationTemplate> templates) {
        linkNameTextField.setText(cat.getLinkName());
        linkValueTextField.setText(cat.getLinkValue());
        String concept = cat.getToConcept();
        if (concept.equals(Association.VALUE_NIL) || concept.equals(Association.VALUE_SELF)) {
            ObservableList<String> items = FXCollections.observableArrayList();
            items.add(concept);
            toConceptComboBox.setItems(items);
        }
        else {
            // Look up the link templates. Find a match linkvalue and set it's toconcept, then select the cat's toConcept
            var opt = templates.stream()
                    .filter(t -> t.getLinkName().equals(cat.getLinkName()))
                    .findFirst();
            if (opt.isPresent()) {
                toConceptComboBoxDecorator.setConcept(opt.get().getToConcept(), cat.getToConcept());
            }
            else {
                toConceptComboBoxDecorator.setConcept(concept);
            }
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
        ResourceBundle i18n = Initializer.getToolBox().getI18nBundle();
        return FXMLUtils.newInstance(AssociationEditorPaneController.class,
                "/fxml/AssociationEditorPane.fxml",
                i18n);
    }
}
