package org.mbari.m3.vars.annotation.ui.roweditor;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListView;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import de.jensd.fx.glyphs.GlyphsFactory;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.m3.vars.annotation.model.ConceptDetails;
import org.mbari.m3.vars.annotation.ui.shared.animation.AutoCompleteComboBoxDecorator;

public class RowEditorPaneController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private JFXButton addButton;

    @FXML
    private JFXButton editButton;

    @FXML
    private JFXButton removeButton;

    @FXML
    private JFXComboBox<String> conceptComboBox;

    @FXML
    private JFXListView<Association> associationListView;

    private EventBus eventBus;

    @FXML
    void onAdd(ActionEvent event) {

    }

    @FXML
    void onAssociationSelected(MouseEvent event) {

    }

    @FXML
    void onEdit(ActionEvent event) {

    }

    @FXML
    void onRemove(ActionEvent event) {

    }

    @FXML
    void initialize() {
        assert addButton != null : "fx:id=\"addButton\" was not injected: check your FXML file 'RowEditorPane.fxml'.";
        assert editButton != null : "fx:id=\"editButton\" was not injected: check your FXML file 'RowEditorPane.fxml'.";
        assert removeButton != null : "fx:id=\"removeButton\" was not injected: check your FXML file 'RowEditorPane.fxml'.";
        assert conceptComboBox != null : "fx:id=\"conceptComboBox\" was not injected: check your FXML file 'RowEditorPane.fxml'.";
        assert associationListView != null : "fx:id=\"associationListView\" was not injected: check your FXML file 'RowEditorPane.fxml'.";

        eventBus = Initializer.getToolBox().getEventBus();

        GlyphsFactory gf = MaterialIconFactory.get();
        Text deleteIcon = gf.createIcon(MaterialIcon.DELETE);
        addButton.setText(null);
        addButton.setGraphic(deleteIcon);
        Text editIcon = gf.createIcon(MaterialIcon.EDIT);
        editButton.setText(null);
        editButton.setGraphic(editIcon);
        Text addIcon = gf.createIcon(MaterialIcon.ADD);
        addButton.setText(null);
        addButton.setGraphic(addIcon);

        initConceptComboBox();

    }

    private void initConceptComboBox() {
        UIToolBox toolBox = Initializer.getToolBox();

        // -- Configure combobox autocomplete
        new AutoCompleteComboBoxDecorator<>(conceptComboBox);
        toolBox.getServices()
                .getConceptService()
                .findAllNames()
                .thenApply(names -> {
                    ObservableList<String> cns = FXCollections.observableArrayList(names);
                    Platform.runLater(() -> {
                        conceptComboBox.setItems(cns);
                    });
                    return null;
                });

        // -- Listen for selected annotaitons
        ObservableList<Annotation> selectedAnnotations = toolBox.getData()
                .getSelectedAnnotations();

        selectedAnnotations.addListener((ListChangeListener.Change<? extends Annotation> change) -> {
            if (selectedAnnotations.size() == 1) {
                conceptComboBox.setEditable(true);
                String value = selectedAnnotations.stream()
                        .findFirst()
                        .map(Annotation::getConcept)
                        .get();
                conceptComboBox.setValue(value);
            }
            else {
                toolBox.getServices()
                        .getConceptService()
                        .findRootDetails()
                        .thenApply(root -> {
                            Platform.runLater(() -> {
                                conceptComboBox.setValue(root.getName());
                                conceptComboBox.setEditable(false);
                            });
                            return null;
                        });
            }
            conceptComboBox.getEditor().selectAll();
        });
    }
}
