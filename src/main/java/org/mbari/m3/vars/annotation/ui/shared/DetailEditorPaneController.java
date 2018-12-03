package org.mbari.m3.vars.annotation.ui.shared;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import io.reactivex.rxjavafx.observables.JavaFxObservable;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.m3.vars.annotation.model.Details;
import org.mbari.m3.vars.annotation.util.FXMLUtils;

import javax.annotation.Nullable;
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
    private JFXTextField linkNameTextField;

    @FXML
    private JFXComboBox<String> toConceptComboBox;

    @FXML
    private JFXTextField linkValueTextField;

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

    public JFXTextField getLinkNameTextField() {
        return linkNameTextField;
    }

    public JFXComboBox<String> getToConceptComboBox() {
        return toConceptComboBox;
    }

    public JFXTextField getLinkValueTextField() {
        return linkValueTextField;
    }

    public void setDetails(@Nullable Details details) {
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

        DetailEditorPaneController controller = FXMLUtils.newInstance(DetailEditorPaneController.class,
                "/fxml/DetailEditorPane.fxml");
        controller.toConceptComboBoxDecorator = new HierarchicalConceptComboBoxDecorator(controller.toConceptComboBox,
                toolBox.getServices().getConceptService());
        return controller;
    }
}
