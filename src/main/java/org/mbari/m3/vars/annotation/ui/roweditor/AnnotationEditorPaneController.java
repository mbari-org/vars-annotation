package org.mbari.m3.vars.annotation.ui.roweditor;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListView;
import java.net.URL;
import java.util.*;

import de.jensd.fx.glyphs.GlyphsFactory;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.commands.DeleteAssociationsCmd;
import org.mbari.m3.vars.annotation.commands.UpdateAnnotationCmd;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.m3.vars.annotation.ui.shared.FilteredComboBoxDecorator;

public class AnnotationEditorPaneController {

    @FXML
    private BorderPane root;

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

    private final ObjectProperty<Annotation> annotation = new SimpleObjectProperty<>();

    public BorderPane getRoot() {
        return root;
    }

    public JFXButton getAddButton() {
        return addButton;
    }

    public JFXButton getEditButton() {
        return editButton;
    }

    public JFXButton getRemoveButton() {
        return removeButton;
    }

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
        if (!items.isEmpty() && annotation.get() != null) {
            Map<Association, UUID> map = new HashMap<>();
            map.put(items.get(0), annotation.get().getObservationUuid());
            eventBus.send(new DeleteAssociationsCmd(map));
        }
    }

    @FXML
    void initialize() {

        // -- Make buttons pretty
        GlyphsFactory gf = MaterialIconFactory.get();
        Text deleteIcon = gf.createIcon(MaterialIcon.DELETE, "30px");
        removeButton.setText(null);
        removeButton.setGraphic(deleteIcon);
        removeButton.setDisable(true);
        removeButton.defaultButtonProperty().bind(removeButton.focusedProperty()); // Enter triggers button
        Text editIcon = gf.createIcon(MaterialIcon.EDIT, "30px");
        editButton.setText(null);
        editButton.setGraphic(editIcon);
        editButton.setDisable(true);
        editButton.defaultButtonProperty().bind(editButton.focusedProperty()); // Enter triggers button
        Text addIcon = gf.createIcon(MaterialIcon.ADD, "30px");
        addButton.setText(null);
        addButton.setGraphic(addIcon);
        addButton.setDisable(true);
        addButton.defaultButtonProperty().bind(addButton.focusedProperty()); // Enter triggers button

        // Enable buttons based on state
        addButton.disableProperty().bind(annotation.isNull());

        // -- If no association is selected disable edit and remove buttons
        associationListView.setCellFactory(lv -> new AssociationCell());
        associationListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        associationListView.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldv, newv) -> {
                    boolean disable = newv == null;
                    editButton.setDisable(disable);
                    removeButton.setDisable(disable);
                });

        // -- Configure combobox autocomplete
        //new AutoCompleteComboBoxListener<>(conceptComboBox);
        new FilteredComboBoxDecorator<>(conceptComboBox, FilteredComboBoxDecorator.STARTSWITH_IGNORE_SPACES);
        conceptComboBox.setEditable(false);
        conceptComboBox.setOnKeyReleased(v -> {
            if (v.getCode() == KeyCode.ENTER) {
                String item = conceptComboBox.getValue();
                if (item != null &&
                        annotation.get() != null &&
                        !annotation.get().getConcept().equals(item)) {

                    toolBox.getServices()
                            .getConceptService()
                            .findConcept(item)
                            .thenAccept(opt -> opt.ifPresent(concept -> {
                                String primaryName = concept.getName();
                                //if (!primaryName.equals(item)) {
                                    // Change to the primary name. Fire off updated event
                                    Annotation oldA = this.annotation.get();
                                    Annotation newA = new Annotation(oldA);
                                    newA.setConcept(primaryName);
                                    Platform.runLater(() -> {
                                        conceptComboBox.getSelectionModel().select(primaryName);
                                        eventBus.send(new UpdateAnnotationCmd(oldA, newA));
                                    });
                                //}
                            }));
                }
            }
        });
        loadComboBoxData();



        // If the cache is cleared reload combobox data
        // TODO there's a bug in FilteredComboBoxDecorator that causes filtering to fail after refresh
//        eventBus.toObserverable()
//                .ofType(ClearCacheMsg.class)
//                .subscribe(c -> loadComboBoxData());

        setAnnotation(null);
    }

    public ObservableList<Association> getSelectedAssociations() {
        return associationListView.getSelectionModel().getSelectedItems();
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation.set(annotation);
        boolean isNull = annotation == null;

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
                conceptComboBox.getSelectionModel().select(annotation.getConcept());
                ObservableList<Association> ass = FXCollections.observableArrayList(annotation.getAssociations());
                associationListView.setItems(ass);
                requestFocus();
            });
        }

    }

    public void requestFocus() {
        conceptComboBox.requestFocus();
    }

    private void loadComboBoxData() {

        toolBox.getServices()
                .getConceptService()
                .findAllNames()
                .thenAccept(names -> {
                    FilteredList<String> cns = new FilteredList<>(FXCollections.observableArrayList(names));
                    Platform.runLater(() -> conceptComboBox.setItems(cns));
                });
    }

    public static AnnotationEditorPaneController newInstance() {
        final ResourceBundle bundle = Initializer.getToolBox().getI18nBundle();
        FXMLLoader loader = new FXMLLoader(AnnotationEditorPaneController.class
                .getResource("/fxml/AnnotationEditorPane.fxml"), bundle);

        try {
            loader.load();
            return loader.getController();
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to load AnnotationEditorPane from FXML", e);
        }
    }

    class AssociationCell extends ListCell<Association> {
        private final Label label = new Label();
        private final Tooltip tooltip = new Tooltip();

        public AssociationCell() {
            setTooltip(tooltip);
            setGraphic(label);
        }

        @Override
        protected void updateItem(Association item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null) {
                label.setText(null);
                setTooltip(null);
            }
            else {
                String s = item.toString();
                label.setText(s);
                tooltip.setText(s);
            }
        }
    }

}
