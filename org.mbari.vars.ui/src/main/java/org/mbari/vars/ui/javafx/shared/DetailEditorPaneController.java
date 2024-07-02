package org.mbari.vars.ui.javafx.shared;


import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.mbari.vars.ui.Initializer;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.services.model.Association;
import org.mbari.vars.services.model.Details;
import org.mbari.vars.ui.util.FXMLUtils;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * @author Brian Schlining
 * @since 2018-11-27T15:30:00
 */
public class DetailEditorPaneController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private GridPane root;

    @FXML
    private TextField linkNameTextField;

    @FXML
    private ComboBox<String> toConceptComboBox;

    @FXML
    private TextField linkValueTextField;

    private HierarchicalConceptComboBoxDecorator toConceptComboBoxDecorator;


    @FXML
    void initialize() {
        // Add filtering of toConcepts
        new FilteredComboBoxDecorator<>(toConceptComboBox, FilteredComboBoxDecorator.STARTSWITH_IGNORE_SPACES);
        linkNameTextField.setDisable(true);

    }



    public GridPane getRoot() {
        return root;
    }

    public TextField getLinkNameTextField() {
        return linkNameTextField;
    }

    public ComboBox<String> getToConceptComboBox() {
        return toConceptComboBox;
    }

    public TextField getLinkValueTextField() {
        return linkValueTextField;
    }

    /**
     *
     * @param details This can be null
     */
    public void setDetails(Details details) {
        if (details == null) {
            linkNameTextField.setText(null);
            toConceptComboBox.getItems().clear();
            toConceptComboBox.setItems(FXCollections.observableArrayList());
        }
        else {
            linkNameTextField.setText(details.getLinkName());
            toConceptComboBoxDecorator.setConcept(details.getToConcept());
            linkValueTextField.setText(details.getLinkValue());
        }
    }

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

    public static DetailEditorPaneController newInstance(UIToolBox toolBox) {

        ResourceBundle i18n = Initializer.getToolBox().getI18nBundle();
        DetailEditorPaneController controller = FXMLUtils.newInstance(DetailEditorPaneController.class,
                "/fxml/DetailEditorPane.fxml",
                i18n);
        controller.toConceptComboBoxDecorator = new HierarchicalConceptComboBoxDecorator(controller.toConceptComboBox,
                toolBox);
        return controller;
    }
}
