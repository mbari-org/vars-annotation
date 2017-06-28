package org.mbari.m3.vars.annotation.ui.roweditor;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListView;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import de.jensd.fx.glyphs.GlyphsFactory;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
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
import org.mbari.m3.vars.annotation.commands.SelectedAnnotations;
import org.mbari.m3.vars.annotation.commands.UpdateAnnotation;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.m3.vars.annotation.ui.shared.AutoCompleteComboBoxDecorator;

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
    private volatile Annotation annotation;


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

        // -- Make buttons pretty
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

        // -- If no association is selected disable edit and remove buttons
        associationListView.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldv, newv) -> {
                    boolean disable = newv == null;
                    editButton.setDisable(disable);
                    removeButton.setDisable(disable);
                });

        // -- Configure combobox autocomplete
        new AutoCompleteComboBoxDecorator<>(conceptComboBox);
        conceptComboBox.setOnKeyTyped(e -> {
            if (e.getCode().equals(KeyCode.ENTER) && annotation != null) {
                Annotation oldA = this.annotation;
                Annotation newA = new Annotation(oldA);
                newA.setConcept(conceptComboBox.getValue());
                eventBus.send(new UpdateAnnotation(oldA, newA));
                e.consume();
            }
        });
        loadComboBoxData();

        // If the cache is cleared reload combobox data
        eventBus.toObserverable()
                .ofType(ClearCache.class)
                .subscribe(c -> loadComboBoxData());

        // Listen for Annotation selections
        eventBus.toObserverable()
                .ofType(SelectedAnnotations.class)
                .subscribe(sa -> {
                    Annotation a0 = sa.getAnnotations().size() == 1 ? sa.getAnnotations().get(0) : null;
                    setAnnotation(a0);
                });

        setAnnotation(null);
    }

    private void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
        boolean isNull = annotation == null;
        setEnabled(!isNull);

        // -- When an annotation is selected set its associations in the listview
        if (isNull) {
            toolBox.getServices()
                    .getConceptService()
                    .findRootDetails()
                    .thenApply(root -> {
                        Platform.runLater(() -> {
                            conceptComboBox.setValue(root.getName());
                            conceptComboBox.getEditor().selectAll();
                            conceptComboBox.getEditor().requestFocus();
                            associationListView.setItems(null);
                        });
                        return null;
                    });
        }
        else {
            Platform.runLater(() -> {
                conceptComboBox.setValue(annotation.getConcept());
                conceptComboBox.getEditor().selectAll();
                conceptComboBox.getEditor().requestFocus();
                ObservableList<Association> ass = FXCollections.observableArrayList(annotation.getAssociations());
                associationListView.setItems(ass);
            });
        }

    }

    private void setEnabled(boolean enable) {
        boolean disable = !enable;
        addButton.setDisable(disable);
        editButton.setDisable(disable);
        removeButton.setDisable(disable);
        conceptComboBox.setEditable(enable);
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

}
