package org.mbari.m3.vars.annotation.ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.jensd.fx.glyphs.GlyphsFactory;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import io.reactivex.Observable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.commands.*;
import org.mbari.m3.vars.annotation.events.*;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.services.AnnotationService;
import org.mbari.m3.vars.annotation.ui.mediadialog.SelectMediaDialog;
import org.mbari.m3.vars.annotation.ui.shared.ConceptSelectionDialogController;
import org.mbari.m3.vars.annotation.util.FnUtils;
import org.mbari.m3.vars.annotation.util.JFXUtilities;

/**
 *
 */
public class BulkEditorPaneController {

    private UIToolBox toolBox;

    private ConceptSelectionDialogController conceptDialogController;

    private SelectMediaDialog selectMediaDialog;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private VBox root;

    @FXML
    private JFXCheckBox conceptCheckBox;

    @FXML
    private JFXCheckBox associationCheckBox;

    @FXML
    private ComboBox<String> conceptCombobox;

    @FXML
    private ComboBox<Association> associationCombobox;

    @FXML
    private JFXButton refreshButton;

    @FXML
    private JFXButton moveFramesButton;

    @FXML
    private JFXButton renameObservationsButton;

    @FXML
    private JFXButton deleteObservationsButton;

    @FXML
    private JFXButton addAssociationButton;

    @FXML
    private JFXButton replaceAssociationButton;

    @FXML
    private JFXButton deleteAssociationButton;

    @FXML
    private JFXButton searchButton;

    @FXML
    private ComboBox<String> groupComboBox;

    @FXML
    private ComboBox<String> activityComboBox;

    @FXML
    private Label groupLabel;

    @FXML
    private Label activityLabel;

    @FXML
    void initialize() {

        toolBox = Initializer.getToolBox();
        conceptDialogController = new ConceptSelectionDialogController(toolBox);
        toolBox.getServices()
                .getConceptService()
                .findRoot()
                .thenAccept(c -> conceptDialogController.setConcept(c.getName()));

        selectMediaDialog = new SelectMediaDialog(
                toolBox.getServices().getAnnotationService(),
                toolBox.getServices().getMediaService(),
                toolBox.getI18nBundle());
        selectMediaDialog.getDialogPane()
                .getStylesheets()
                .addAll(toolBox.getStylesheets());

        final Observable<Object> obs = toolBox.getEventBus().toObserverable();
        obs.ofType(AnnotationsChangedEvent.class)
                .subscribe(e -> needsRefresh());
        obs.ofType(AnnotationsAddedEvent.class)
                .subscribe(e -> needsRefresh());
        obs.ofType(AnnotationsRemovedEvent.class)
                .subscribe(e -> needsRefresh());
        obs.ofType(MediaChangedEvent.class)
                .subscribe(e -> needsRefresh());

        // --- Configure buttons
        GlyphsFactory gf = MaterialIconFactory.get();
        ResourceBundle i18n = toolBox.getI18nBundle();

        Text refreshIcon = gf.createIcon(MaterialIcon.REFRESH, "30px");
        refreshButton.setGraphic(refreshIcon);
        refreshButton.setDisable(true);
        refreshButton.setTooltip(new Tooltip(i18n.getString("bulkeditor.refresh.tooltip")));
        refreshButton.setOnAction(e -> refresh());

        Image moveAnnoImg = new Image(getClass()
                .getResource("/images/buttons/row_replace.png").toExternalForm());
        moveFramesButton.setGraphic(new ImageView(moveAnnoImg));
        moveFramesButton.setTooltip(new Tooltip(i18n.getString("bulkeditor.annotation.move.tooltip")));
        moveFramesButton.setOnAction(e -> moveAnnotations());

        Image editAnnoImg = new Image(getClass()
                .getResource("/images/buttons/row_edit.png").toExternalForm());
        renameObservationsButton.setGraphic(new ImageView(editAnnoImg));
        renameObservationsButton.setTooltip(new Tooltip(i18n.getString("bulkeditor.annotation.rename.tooltip")));
        renameObservationsButton.setOnAction(e -> renameAnnotations());

        Image deleteAnnoImg = new Image(getClass()
                .getResource("/images/buttons/row_delete.png").toExternalForm());
        deleteObservationsButton.setGraphic(new ImageView(deleteAnnoImg));
        deleteObservationsButton.setTooltip(new Tooltip(i18n.getString("bulkeditor.annotation.delete.tooltip")));
        deleteObservationsButton.setOnAction(e -> deleteAnnotations());

        Image addAssImg = new Image(getClass()
                .getResource("/images/buttons/branch_add.png").toExternalForm());
        addAssociationButton.setGraphic(new ImageView(addAssImg));
        addAssociationButton.setTooltip(new Tooltip(i18n.getString("bulkeditor.association.add.tooltip")));

        Image editAssImg = new Image(getClass()
                .getResource("/images/buttons/branch_edit.png").toExternalForm());
        replaceAssociationButton.setGraphic(new ImageView(editAssImg));
        replaceAssociationButton.setTooltip(new Tooltip(i18n.getString("bulkeditor.association.edit.tooltip")));

        Image deleteAssImg = new Image(getClass()
                .getResource("/images/buttons/branch_delete.png").toExternalForm());
        deleteAssociationButton.setGraphic(new ImageView(deleteAssImg));
        deleteAssociationButton.setTooltip(new Tooltip(i18n.getString("bulkeditor.association.delete.tooltip")));

        Text searchIcon = gf.createIcon(MaterialIcon.SEARCH, "30px");
        searchButton.setText(null);
        searchButton.setGraphic(searchIcon);
        searchButton.setTooltip(new Tooltip(i18n.getString("bulkeditor.search.button")));
        searchButton.setOnAction(e -> search());

        activityLabel.setText(i18n.getString("bulkeditor.activity.label"));
        activityComboBox.setOnAction(e -> changeActivity());

        groupLabel.setText(i18n.getString("bulkeditor.group.label"));
        groupComboBox.setOnAction(e -> changeGroups());

    }

    public VBox getRoot() {
        return root;
    }

    public static BulkEditorPaneController newInstance(UIToolBox toolBox) {
        final ResourceBundle bundle = toolBox.getI18nBundle();
        FXMLLoader loader = new FXMLLoader(BulkEditorPaneController.class
                .getResource("/fxml/BulkEditorPane.fxml"), bundle);
        try {
            loader.load();
            BulkEditorPaneController controller = loader.getController();
            //controller.toolBox = toolBox;
            return controller;
        }
        catch (Exception e) {
            throw  new RuntimeException("Failed to load BulkEditorPane from FXML", e);
        }
    }

    private void refresh() {

        ObservableList<Annotation> annotations = toolBox.getData().getAnnotations();

        List<String> concepts = annotations.stream()
                .map(Annotation::getConcept)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());

        List<Association> associations = annotations.stream()
                .map(Annotation::getAssociations)
                .flatMap(List::stream)
                .filter(FnUtils.distinctBy(Association::toString))
                .sorted(Comparator.comparing(Association::toString))
                .collect(Collectors.toList());

        Platform.runLater(() -> {
            conceptCombobox.setItems(FXCollections.observableArrayList(concepts));
            associationCombobox.setItems(FXCollections.observableArrayList(associations));
        });

        final AnnotationService annotationService = toolBox.getServices().getAnnotationService();
        annotationService
                .findGroups()
                .thenAccept(groups -> Platform.runLater(() ->
                        groupComboBox.setItems(FXCollections.observableArrayList(groups))));
        annotationService
                .findActivities()
                .thenAccept(activities -> Platform.runLater(() ->
                        activityComboBox.setItems(FXCollections.observableArrayList(activities))));

    }

    private void search() {
        boolean searchConcepts = conceptCheckBox.isSelected();
        boolean searchDetails = associationCheckBox.isSelected();
        String concept = conceptCombobox.getSelectionModel().getSelectedItem();
        Association association = associationCombobox.getSelectionModel().getSelectedItem();
        List<Annotation> annotations = new ArrayList<>(toolBox.getData().getAnnotations());
        Predicate<Annotation> nullPredicate = a -> false;
        Predicate<Annotation> conceptPredicate = a -> a.getConcept().equals(concept);
        Predicate<Annotation> associationPredicate = a -> a.getAssociations()
                .stream()
                .anyMatch(ass -> ass.getLinkName().equals(association.getLinkName()) &&
                        ass.getToConcept().equals(association.getToConcept()) &&
                        ass.getLinkValue().equals(association.getLinkValue()));

        Predicate<Annotation> predicate = nullPredicate;

        if (searchConcepts && searchDetails) {
            predicate = conceptPredicate.and(associationPredicate);
        }
        else if (searchConcepts) {
            predicate = conceptPredicate;
        }
        else if (searchDetails) {
            predicate = associationPredicate;
        }

        List<Annotation> foundAnnotations = annotations.stream()
                .filter(predicate)
                .collect(Collectors.toList());

        toolBox.getEventBus()
                .send(new AnnotationsSelectedEvent(foundAnnotations));
    }

    private void needsRefresh() {
        JFXUtilities.runOnFXThread(() -> refreshButton.setDisable(false));
    }

    private void changeGroups() {
        final String group = groupComboBox.getSelectionModel().getSelectedItem();
        final List<Annotation> annotations = new ArrayList<>(toolBox.getData().getSelectedAnnotations());

        ResourceBundle i18n = toolBox.getI18nBundle();
        String title = i18n.getString("bulkeditor.group.dialog.title");
        String header = i18n.getString("bulkeditor.group.dialog.header") + " " + group;
        String content = i18n.getString("bulkeditor.group.dialog.content1") + " " +
                group + " " + i18n.getString("bulkeditor.group.dialog.content2") + " " +
                annotations.size() + " " + i18n.getString("bulkeditor.group.dialog.content3");
        Runnable action = () -> toolBox.getEventBus()
                .send(new ChangeGroupCmd(annotations, group));
        doActionWithAlert(title, header, content, action);
    }

    private void changeActivity() {
        final String activity = activityComboBox.getSelectionModel().getSelectedItem();
        final List<Annotation> annotations = new ArrayList<>(toolBox.getData().getSelectedAnnotations());

        ResourceBundle i18n = toolBox.getI18nBundle();
        String title = i18n.getString("bulkeditor.activity.dialog.title");
        String header = i18n.getString("bulkeditor.activity.dialog.header") + " " + activity;
        String content = i18n.getString("bulkeditor.activity.dialog.content1") + " " +
                activity + " " + i18n.getString("bulkeditor.activity.dialog.content2") + " " +
                annotations.size() + " " + i18n.getString("bulkeditor.activity.dialog.content3");
        Runnable action = () -> toolBox.getEventBus()
                .send(new ChangeActivityCmd(annotations, activity));
        doActionWithAlert(title, header, content, action);
    }

    private void moveAnnotations() {
        // TODO show selection dialog
        final List<Annotation> annotations = new ArrayList<>(toolBox.getData().getSelectedAnnotations());
        Optional<Media> opt = selectMediaDialog.showAndWait();
        opt.ifPresent(media -> toolBox.getEventBus()
                .send(new MoveAnnotationsCmd(annotations, media)));

    }

    private void renameAnnotations() {
        final List<Annotation> annotations = new ArrayList<>(toolBox.getData().getSelectedAnnotations());

        ResourceBundle i18n = toolBox.getI18nBundle();
        String title = i18n.getString("bulkeditor.concept.dialog.title");
        String header = i18n.getString("bulkeditor.concept.dialog.header");
        String content = i18n.getString("bulkeditor.concept.dialog.content1") + " " +
                annotations.size()  + " " + i18n.getString("bulkeditor.concept.dialog.content2");

        Dialog<String> dialog = conceptDialogController.getDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);
        Platform.runLater(() -> conceptDialogController.getComboBox().requestFocus());
        Optional<String> opt = dialog.showAndWait();
        opt.ifPresent(c -> toolBox.getEventBus()
                .send(new ChangeConceptCmd(annotations, c)));
    }

    private void deleteAnnotations() {
        final List<Annotation> annotations = new ArrayList<>(toolBox.getData().getSelectedAnnotations());

        ResourceBundle i18n = toolBox.getI18nBundle();
        String title = i18n.getString("bulkeditor.delete.anno.dialog.title");
        String header = i18n.getString("bulkeditor.delete.anno.dialog.header");
        String content = i18n.getString("bulkeditor.delete.anno.dialog.content1") + " " +
                annotations.size()  + " " + i18n.getString("bulkeditor.delete.anno.dialog.content2");
        Runnable action = () -> toolBox.getEventBus()
                .send(new DeleteAnnotationsCmd(annotations));

        doActionWithAlert(title, header, content, action);
    }

    private void doActionWithAlert(String title, String header, String content, Runnable action) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.getDialogPane().getStylesheets().addAll(toolBox.getStylesheets());
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            action.run();
        }
    }

    private void addAssociations() {}
    private void changeAssociations() {}
    private void deleteAssociations() {}
}
