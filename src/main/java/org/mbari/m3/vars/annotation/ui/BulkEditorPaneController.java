package org.mbari.m3.vars.annotation.ui;

import com.google.common.base.Preconditions;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;

import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.jensd.fx.glyphs.GlyphsFactory;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import io.reactivex.Observable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.commands.*;
import org.mbari.m3.vars.annotation.events.*;
import org.mbari.m3.vars.annotation.model.*;
import org.mbari.m3.vars.annotation.services.AnnotationService;
import org.mbari.m3.vars.annotation.services.ConceptService;
import org.mbari.m3.vars.annotation.ui.mediadialog.SelectMediaDialog;
import org.mbari.m3.vars.annotation.ui.shared.ConceptSelectionDialogController;
import org.mbari.m3.vars.annotation.ui.shared.DetailsDialog;
import org.mbari.m3.vars.annotation.ui.shared.FilteredComboBoxDecorator;
import org.mbari.m3.vars.annotation.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 *
 */
public class BulkEditorPaneController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private ObservableList<Annotation> annotations = FXCollections.emptyObservableList();
    private ObservableList<Annotation> selectedAnnotations = FXCollections.emptyObservableList();
    private EventBus eventBus;

    private final EventHandler<ActionEvent> noopHandler = (event) -> {};
    private final EventHandler<ActionEvent> activityHandler = (event) -> changeActivity();
    private final EventHandler<ActionEvent> groupHandler = (event) -> changeGroups();

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
    private Label searchLabel;

    private ListChangeListener<Annotation> selectionChangeListener;



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

        // --- Configure buttons
        initializeButtons();

        // --- Configure labels
        ResourceBundle i18n = toolBox.getI18nBundle();
        activityLabel.setText(i18n.getString("bulkeditor.activity.label"));
        activityComboBox.setOnAction(noopHandler);

        groupLabel.setText(i18n.getString("bulkeditor.group.label"));
        groupComboBox.setOnAction(noopHandler);

        // --- Configure comboboxes
        new FilteredComboBoxDecorator<>(associationCombobox,
                (txt, assoc) -> StringUtils.containsOrderedChars(txt.toUpperCase(), assoc.toString().toUpperCase()));
        associationCombobox.setEditable(false);

        new FilteredComboBoxDecorator<>(conceptCombobox,
                FilteredComboBoxDecorator.STARTSWITH_IGNORE_SPACES);
        conceptCombobox.setEditable(false);

        // --- Enable/Disable buttons as appropriate
        selectionChangeListener = c -> {
            boolean disable = selectedAnnotations.size() == 0;
            moveFramesButton.setDisable(disable);
            addAssociationButton.setDisable(disable);
            deleteObservationsButton.setDisable(disable);
            renameObservationsButton.setDisable(disable);
            replaceAssociationButton.setDisable(true);
            deleteAssociationButton.setDisable(disable);
        };

        selectedAnnotations.addListener(selectionChangeListener);

        // --- Updated search string in UI
        Runnable updateSearchLabelFn = () -> {
            StringBuffer sb = new StringBuffer();
            if (conceptCheckBox.isSelected()
                    && !conceptCombobox.getSelectionModel().isEmpty()) {
                String concept = conceptCombobox.getSelectionModel().getSelectedItem();
                sb.append(concept);
            }

            if (associationCheckBox.isSelected()
                    && !associationCombobox.getSelectionModel().isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(" with ");
                }
                sb.append(associationCombobox.getSelectionModel().getSelectedItem());
            }
            if (sb.length() == 0) {
                String text = toolBox.getI18nBundle().getString("bulkeditor.search.defaultlabel");
                searchLabel.setText(text);
                searchButton.setDisable(true);
            }
            else {
                searchLabel.setText(sb.toString());
                searchButton.setDisable(false);
            }
        };
        updateSearchLabelFn.run(); // Set the defaults

        conceptCheckBox.selectedProperty()
                .addListener((obs, oldv, newv) -> updateSearchLabelFn.run());
        conceptCombobox.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldv, newv) -> updateSearchLabelFn.run());
        associationCheckBox.selectedProperty()
                .addListener((obs, oldv, newv) -> updateSearchLabelFn.run());
        associationCombobox.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldv, newv) -> updateSearchLabelFn.run());

    }


    private void initializeButtons() {
        // --- Configure buttons
        GlyphsFactory gf = MaterialIconFactory.get();
        ResourceBundle i18n = toolBox.getI18nBundle();

        Image moveAnnoImg = new Image(getClass()
                .getResource("/images/buttons/row_replace.png").toExternalForm());
        moveFramesButton.setGraphic(new ImageView(moveAnnoImg));
        moveFramesButton.setTooltip(new Tooltip(i18n.getString("bulkeditor.annotation.move.tooltip")));
        moveFramesButton.setOnAction(e -> moveAnnotations());
        moveFramesButton.setDisable(true);

        Image editAnnoImg = new Image(getClass()
                .getResource("/images/buttons/row_edit.png").toExternalForm());
        renameObservationsButton.setGraphic(new ImageView(editAnnoImg));
        renameObservationsButton.setTooltip(new Tooltip(i18n.getString("bulkeditor.annotation.rename.tooltip")));
        renameObservationsButton.setOnAction(e -> renameAnnotations());
        renameObservationsButton.setDisable(true);

        Image deleteAnnoImg = new Image(getClass()
                .getResource("/images/buttons/row_delete.png").toExternalForm());
        deleteObservationsButton.setGraphic(new ImageView(deleteAnnoImg));
        deleteObservationsButton.setTooltip(new Tooltip(i18n.getString("bulkeditor.annotation.delete.tooltip")));
        deleteObservationsButton.setOnAction(e -> deleteAnnotations());
        deleteObservationsButton.setDisable(true);

        Image addAssImg = new Image(getClass()
                .getResource("/images/buttons/branch_add.png").toExternalForm());
        addAssociationButton.setGraphic(new ImageView(addAssImg));
        addAssociationButton.setTooltip(new Tooltip(i18n.getString("bulkeditor.association.add.tooltip")));
        addAssociationButton.setOnAction(e -> addAssociations());
        addAssociationButton.setDisable(true);

        Image editAssImg = new Image(getClass()
                .getResource("/images/buttons/branch_edit.png").toExternalForm());
        replaceAssociationButton.setGraphic(new ImageView(editAssImg));
        replaceAssociationButton.setTooltip(new Tooltip(i18n.getString("bulkeditor.association.edit.tooltip")));
        replaceAssociationButton.setDisable(true);

        Image deleteAssImg = new Image(getClass()
                .getResource("/images/buttons/branch_delete.png").toExternalForm());
        deleteAssociationButton.setGraphic(new ImageView(deleteAssImg));
        deleteAssociationButton.setTooltip(new Tooltip(i18n.getString("bulkeditor.association.delete.tooltip")));
        deleteAssociationButton.setDisable(true);
        deleteAssociationButton.setOnAction(e -> deleteAssociations());

        Text searchIcon = gf.createIcon(MaterialIcon.SEARCH, "30px");
        searchButton.setText(null);
        searchButton.setGraphic(searchIcon);
        searchButton.setTooltip(new Tooltip(i18n.getString("bulkeditor.search.button")));
        searchButton.setOnAction(e -> search());
        searchButton.setDisable(true);
        searchIcon.getStyleClass().add("attention-icon");
    }

    public VBox getRoot() {
        return root;
    }

    /**
     *
     * @param toolBox
     * @param annotations
     * @param selectedAnnotations
     * @param eventBus
     * @return
     */
    public static BulkEditorPaneController newInstance(UIToolBox toolBox,
                                                       ObservableList<Annotation> annotations,
                                                       ObservableList<Annotation> selectedAnnotations,
                                                       EventBus eventBus) {
        final ResourceBundle bundle = toolBox.getI18nBundle();
        FXMLLoader loader = new FXMLLoader(BulkEditorPaneController.class
                .getResource("/fxml/BulkEditorPane.fxml"), bundle);
        try {
            loader.load();
            BulkEditorPaneController controller = loader.getController();
            controller.setEventBus(eventBus);
            controller.setAnnotations(annotations);
            controller.setSelectedAnnotations(selectedAnnotations);
            return controller;
        }
        catch (Exception e) {
            throw  new RuntimeException("Failed to load BulkEditorPane from FXML", e);
        }
    }

    private void setSelectedAnnotations(@Nonnull  ObservableList<Annotation> selectedAnnotations) {
        Preconditions.checkNotNull(selectedAnnotations);
        if (this.selectedAnnotations != null) {
            selectedAnnotations.removeListener(selectionChangeListener);
        }
        selectedAnnotations.addListener(selectionChangeListener);
        this.selectedAnnotations = selectedAnnotations;
    }

    private void setAnnotations(ObservableList<Annotation> annotations) {
        Preconditions.checkNotNull(annotations);
        this.annotations = annotations;
    }

    private void setEventBus(EventBus eventBus) {
        Preconditions.checkNotNull(eventBus);
        final Observable<Object> obs = eventBus.toObserverable();
        obs.ofType(AnnotationsChangedEvent.class)
                .subscribe(e -> refresh());
        obs.ofType(AnnotationsAddedEvent.class)
                .subscribe(e -> refresh());
        obs.ofType(AnnotationsRemovedEvent.class)
                .subscribe(e -> refresh());
        obs.ofType(MediaChangedEvent.class)
                .subscribe(e -> refresh());
        this.eventBus = eventBus;
    }

    public void refresh() {

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
                .thenAccept(groups -> Platform.runLater(() -> {
                    // Remove the actionhandler or it gets triggered when we set the items
                    groupComboBox.setOnAction(noopHandler);
                    groupComboBox.setItems(FXCollections.observableArrayList(groups));
                    groupComboBox.setOnAction(groupHandler);
                }));
        annotationService
                .findActivities()
                .thenAccept(activities -> Platform.runLater(() -> {
                    // Remove the actionhandler or it gets triggered when we set the items
                    activityComboBox.setOnAction(noopHandler);
                    activityComboBox.setItems(FXCollections.observableArrayList(activities));
                    activityComboBox.setOnAction(activityHandler);
                }));

    }

    private void search() {
        boolean searchConcepts = conceptCheckBox.isSelected();
        boolean searchDetails = associationCheckBox.isSelected();
        String concept = conceptCombobox.getSelectionModel().getSelectedItem();
        Association association = associationCombobox.getSelectionModel().getSelectedItem();
        log.info("Selected concept = " + concept + "\nSelected Association = " + association);
        Predicate<Annotation> nullPredicate = a -> false;
        Predicate<Annotation> conceptPredicate = a -> a.getConcept().equals(concept);
        Predicate<Annotation> associationPredicate = a -> a.getAssociations()
                .stream()
                .anyMatch(ass -> ass != null &&
                        association != null &&
                        ass.getLinkName().equals(association.getLinkName()) &&
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

        eventBus.send(new AnnotationsSelectedEvent(foundAnnotations));
    }


    private void changeGroups() {
        final String group = groupComboBox.getSelectionModel().getSelectedItem();
        final List<Annotation> annosCopy = new ArrayList<>(selectedAnnotations);

        Set<String> groups = annosCopy.stream()
                .map(Annotation::getGroup)
                .collect(Collectors.toSet());

        if (group != null && (groups.size() > 1 || !groups.contains(group))) {

            ResourceBundle i18n = toolBox.getI18nBundle();
            String title = i18n.getString("bulkeditor.group.dialog.title");
            String header = i18n.getString("bulkeditor.group.dialog.header") + " " + group;
            String content = i18n.getString("bulkeditor.group.dialog.content1") + " " +
                    group + " " + i18n.getString("bulkeditor.group.dialog.content2") + " " +
                    annosCopy.size() + " " + i18n.getString("bulkeditor.group.dialog.content3");
            Runnable action = () -> eventBus.send(new ChangeGroupCmd(annosCopy, group));
            doActionWithAlert(title, header, content, action);
        }
    }

    private void changeActivity() {
        final String activity = activityComboBox.getSelectionModel().getSelectedItem();
        final List<Annotation> annosCopy = new ArrayList<>(selectedAnnotations);

        Set<String> activities = annosCopy.stream()
                .map(Annotation::getActivity)
                .collect(Collectors.toSet());

        if (activity != null && (activities.size() > 1 || !activities.contains(activity))) {

            ResourceBundle i18n = toolBox.getI18nBundle();
            String title = i18n.getString("bulkeditor.activity.dialog.title");
            String header = i18n.getString("bulkeditor.activity.dialog.header") + " " + activity;
            String content = i18n.getString("bulkeditor.activity.dialog.content1") + " " +
                    activity + " " + i18n.getString("bulkeditor.activity.dialog.content2") + " " +
                    annosCopy.size() + " " + i18n.getString("bulkeditor.activity.dialog.content3");
            Runnable action = () -> eventBus.send(new ChangeActivityCmd(annosCopy, activity));
            doActionWithAlert(title, header, content, action);
        }
    }

    private void moveAnnotations() {
        final List<Annotation> annosCopy = new ArrayList<>(selectedAnnotations);
        Optional<Media> opt = selectMediaDialog.showAndWait();
        //opt.ifPresent(media -> eventBus.send(new MoveAnnotationsCmd(annosCopy, media)));
        opt.ifPresent(media -> eventBus.send(new MoveAnnotationsAndImagesCmd(annosCopy, media)));
    }

    private void renameAnnotations() {
        final List<Annotation> annosCopy = new ArrayList<>(selectedAnnotations);

        ResourceBundle i18n = toolBox.getI18nBundle();
        String title = i18n.getString("bulkeditor.concept.dialog.title");
        String header = i18n.getString("bulkeditor.concept.dialog.header");
        String content = i18n.getString("bulkeditor.concept.dialog.content1") + " " +
                annosCopy.size()  + " " + i18n.getString("bulkeditor.concept.dialog.content2");

        Dialog<String> dialog = conceptDialogController.getDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);
        Platform.runLater(() -> conceptDialogController.getComboBox().requestFocus());
        Optional<String> opt = dialog.showAndWait();
        opt.ifPresent(c -> eventBus.send(new ChangeConceptCmd(annosCopy, c)));
    }

    private void deleteAnnotations() {
        final List<Annotation> annosCopy = new ArrayList<>(selectedAnnotations);

        ResourceBundle i18n = toolBox.getI18nBundle();
        String title = i18n.getString("bulkeditor.delete.anno.dialog.title");
        String header = i18n.getString("bulkeditor.delete.anno.dialog.header");
        String content = i18n.getString("bulkeditor.delete.anno.dialog.content1") + " " +
                annosCopy.size()  + " " + i18n.getString("bulkeditor.delete.anno.dialog.content2");
        Runnable action = () -> eventBus.send(new DeleteAnnotationsCmd(annosCopy));

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


    private void addAssociations() {
        // --- Get templates that are common to all concepts
        final List<Annotation> annosCopy = new ArrayList<>(selectedAnnotations);
        List<String> concepts = selectedAnnotations.stream()
                .map(Annotation::getConcept)
                .distinct()
                .collect(Collectors.toList());


        lookupTemplates(concepts)
                .whenComplete((templates, ex) -> {
                    if (ex != null) {
                        // TODO show error dialog
                    }
                    else {
                        DetailsDialog dialog = new DetailsDialog(toolBox);
                        dialog.getController().setTemplates(templates);
                        Platform.runLater(() -> {
                            dialog.getDialogPane().getScene().getWindow().sizeToScene();
                            Optional<Details> opt = dialog.showAndWait();
                            opt.ifPresent(details -> {
                                Association a = Association.fromDetails(details);
                                toolBox.getEventBus()
                                        .send(new CreateAssociationsCmd(a, annosCopy));
                            } );
                        });

                        // Request focus in the search text field. We can't do this
                        // until the dialog is visible.
                        toolBox.getExecutorService()
                                .submit(() -> Platform.runLater(() ->
                                        dialog.getController()
                                                .getSearchTextField()
                                                .requestFocus()));
                    }
                });




    }
    private void changeAssociations() {}
    private void deleteAssociations() {
            // Get selected association
        Association association = associationCombobox.getSelectionModel().getSelectedItem();
        Details selectedDetails = association == null ? null : new ConceptAssociationTemplate(association);

        // --- Get all associations in selected rows;
        List<Annotation> annosCopy = new ArrayList<>(selectedAnnotations);
        List<Details> details = selectedAnnotations.stream()
                .flatMap(a -> a.getAssociations().stream())
                .map(ConceptAssociationTemplate::new)
                .collect(Collectors.toList());
        if (!details.contains(selectedDetails)) {
            selectedDetails = null;
        }
        ChoiceDialog<Details> dialog = new ChoiceDialog<>(selectedDetails, details);
        dialog.getDialogPane().getStylesheets().addAll(toolBox.getStylesheets());
        ResourceBundle i18n = toolBox.getI18nBundle();
        dialog.setTitle(i18n.getString("bulkeditor.delete.assoc.dialog.title"));
        dialog.setHeaderText(i18n.getString("bulkeditor.delete.assoc.dialog.header"));
        dialog.setContentText(i18n.getString("bulkeditor.delete.assoc.dialog.content"));
        Optional<Details> detailsToDelete = dialog.showAndWait();
        detailsToDelete.ifPresent(d -> {
            Map<Association, UUID> map = new HashMap<>();
            for (Annotation a : annosCopy) {
                for (Association ass: a.getAssociations()) {
                    if (ass.getLinkName().equals(d.getLinkName())
                            && ass.getToConcept().equals(d.getToConcept())
                            && ass.getLinkValue().equals(d.getLinkValue())) {
                        map.put(ass, a.getObservationUuid());
                    }
                }
            }

            if (!map.isEmpty()) {
                toolBox.getEventBus()
                        .send(new DeleteAssociationsCmd(map));
            }
        });

    }

    private CompletableFuture<List<ConceptAssociationTemplate>> lookupTemplates(List<String> concepts) {
        CompletableFuture<List<ConceptAssociationTemplate>> future = new CompletableFuture<>();
        List<ConceptAssociationTemplate> cats = new CopyOnWriteArrayList<>();
        ConceptService conceptService = toolBox.getServices().getConceptService();
        Observable<List<ConceptAssociationTemplate>> observable = AsyncUtils.observeAll(concepts, conceptService::findTemplates);
        observable.subscribe(cs -> {
                    if (cats.isEmpty()) {
                        cats.addAll(cs);
                    }
                    else {
                        List<ConceptAssociationTemplate> intersection = ListUtils.intersection(cats, cs);
                        cats.clear();
                        cats.addAll(intersection);
                    }
                },
                future::completeExceptionally,
                () -> future.complete(cats));
        return future;
    }
}
