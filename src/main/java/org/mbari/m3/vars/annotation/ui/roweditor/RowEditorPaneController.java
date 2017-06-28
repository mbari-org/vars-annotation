package org.mbari.m3.vars.annotation.ui.roweditor;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListView;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.commands.ClearCache;
import org.mbari.m3.vars.annotation.commands.DeleteAssociations;
import org.mbari.m3.vars.annotation.commands.UpdateAnnotation;
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

    private final UIToolBox toolBox = Initializer.getToolBox();
    private final EventBus eventBus = toolBox.getEventBus();



    @FXML
    void onAdd(ActionEvent event) {
        // TODO Show association editor panel
    }

    @FXML
    void onAssociationSelected(MouseEvent event) {

    }

    @FXML
    void onEdit(ActionEvent event) {
        // TODO show association editor panel
    }

    @FXML
    void onRemove(ActionEvent event) {
        final List<Association> items = new ArrayList<>(associationListView.getSelectionModel().getSelectedItems());
        eventBus.send(new DeleteAssociations(items));
    }

    @FXML
    void initialize() {
        assert addButton != null : "fx:id=\"addButton\" was not injected: check your FXML file 'RowEditorPane.fxml'.";
        assert editButton != null : "fx:id=\"editButton\" was not injected: check your FXML file 'RowEditorPane.fxml'.";
        assert removeButton != null : "fx:id=\"removeButton\" was not injected: check your FXML file 'RowEditorPane.fxml'.";
        assert conceptComboBox != null : "fx:id=\"conceptComboBox\" was not injected: check your FXML file 'RowEditorPane.fxml'.";
        assert associationListView != null : "fx:id=\"associationListView\" was not injected: check your FXML file 'RowEditorPane.fxml'.";

        // If the cache is cleared reload combobox data
        eventBus.toObserverable()
                .ofType(ClearCache.class)
                .subscribe(c -> loadComboBoxData());



        initAssociationListView();
        initAssociationButtons();
        initConceptComboBox();
    }

    private void initAssociationButtons() {
        UIToolBox toolBox = Initializer.getToolBox();

        GlyphsFactory gf = MaterialIconFactory.get();
        Text deleteIcon = gf.createIcon(MaterialIcon.DELETE);
        removeButton.setText(null);
        removeButton.setGraphic(deleteIcon);
        Text editIcon = gf.createIcon(MaterialIcon.EDIT);
        editButton.setText(null);
        editButton.setGraphic(editIcon);
        Text addIcon = gf.createIcon(MaterialIcon.ADD);
        addButton.setText(null);
        addButton.setGraphic(addIcon);

        // -- If no annotation is selected disable all association buttons
        ObservableList<Annotation> selectedAnnotations = toolBox.getData()
                .getSelectedAnnotations();

        selectedAnnotations.addListener((ListChangeListener.Change<? extends Annotation> change) -> {
            boolean disable = selectedAnnotations.size() != 1;
            addButton.setDisable(disable);
            editButton.setDisable(disable);
            removeButton.setDisable(disable);
        });

    }

    private void loadComboBoxData() {
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
    }

    private void initAssociationListView() {
        // -- If no association is selected disable edit and remove buttons
        associationListView.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldv, newv) -> {
                    boolean disable = newv == null;
                    editButton.setDisable(disable);
                    removeButton.setDisable(disable);
                });

        // -- When an annotation is selected set its associations in the listview
        ObservableList<Annotation> selectedAnnotations = toolBox.getData().getSelectedAnnotations();
        selectedAnnotations.addListener((ListChangeListener.Change<? extends Annotation> change) -> {
            if (selectedAnnotations.size() == 1) {
                Annotation a = selectedAnnotations.get(0);
                ObservableList<Association> ass = FXCollections.observableArrayList(a.getAssociations());
                associationListView.setItems(ass);
            }
        });
    }

    private void initConceptComboBox() {


        // -- Configure combobox autocomplete
        new AutoCompleteComboBoxDecorator<>(conceptComboBox);
        loadComboBoxData();

        // -- Listen for selected annotations
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
            conceptComboBox.getEditor().requestFocus();
        });

        conceptComboBox.setOnKeyTyped(e -> {
            if (e.getCode().equals(KeyCode.ENTER) && selectedAnnotations.size() == 1) {
                Annotation oldA = selectedAnnotations.get(0);
                Annotation newA = new Annotation(oldA);
                newA.setConcept(conceptComboBox.getValue());
                eventBus.send(new UpdateAnnotation(oldA, newA));
                e.consume();
            }
        });
    }
}
