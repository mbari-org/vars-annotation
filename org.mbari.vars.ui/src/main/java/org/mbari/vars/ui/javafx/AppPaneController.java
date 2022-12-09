package org.mbari.vars.ui.javafx;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTabPane;
import io.reactivex.rxjava3.core.Observable;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.StatusBar;
import org.mbari.vars.core.EventBus;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.events.AnnotationsSelectedEvent;
import org.mbari.vars.ui.events.MediaChangedEvent;
import org.mbari.vars.ui.events.UserAddedEvent;
import org.mbari.vars.ui.events.UserChangedEvent;
import org.mbari.vars.ui.javafx.mediadialog.MediaDescriptionEditorPane2Controller;
import org.mbari.vars.ui.javafx.mlstage.MachineLearningStageController;
import org.mbari.vars.ui.mediaplayers.ships.MediaParams;
import org.mbari.vars.ui.mediaplayers.ships.OpenRealTimeDialog;
import org.mbari.vars.ui.mediaplayers.ships.OpenRealTimeService;
import org.mbari.vars.ui.mediaplayers.vcr.OpenTapeDialog;
import org.mbari.vars.ui.messages.*;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Media;
import org.mbari.vars.services.model.User;
import org.mbari.vars.services.AnnotationService;
import org.mbari.vars.ui.services.FileBrowsingDecorator;
import org.mbari.vars.ui.javafx.annotable.AnnotationTableController;
import org.mbari.vars.ui.javafx.cbpanel.ConceptButtonPanesController;
import org.mbari.vars.ui.javafx.concepttree.SearchTreePaneController;
import org.mbari.vars.ui.javafx.deployeditor.AnnotationViewController;
import org.mbari.vars.ui.javafx.mediadialog.MediaPaneController;
import org.mbari.vars.ui.javafx.mediadialog.SelectMediaDialog;
import org.mbari.vars.ui.javafx.prefs.PreferencesDialogController;
import org.mbari.vars.ui.javafx.shared.FilteredComboBoxDecorator;
import org.mbari.vars.ui.javafx.userdialog.CreateUserDialog;
import org.mbari.vars.ui.swing.annotable.JXAnnotationTableController;
import org.mbari.vars.ui.util.JFXUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-08-28T16:04:00
 */
public class AppPaneController {

    private BorderPane root;
    private SplitPane masterPane;
    private SplitPane topPane;
    private SplitPane bottomPane;
    private TabPane tabPane;
//    private final AnnotationTableController annotationTableController;
    private final JXAnnotationTableController annotationTableController;
    private ToolBar toolBar;
    private final UIToolBox toolBox;
    private ComboBox<String> usersComboBox;
    ComboBox<String> groupCombobox = new JFXComboBox<>();
    ComboBox<String> activityCombobox = new JFXComboBox<>();
    private CheckBox showConcurrentCheckBox;
    private CheckBox showJsonAssociationsCheckBox;
    private CheckBox showCurrentGroupOnlyCheckBox;
    private PopOver openPopOver;
    private StatusBar utilityPane;
    private final ImageViewController2 imageViewController;
    private final PreferencesDialogController preferencesDialogController;
    private final SelectMediaDialog selectMediaDialog;
    private final OpenRealTimeDialog realTimeDialog;
    private final OpenTapeDialog tapeDialog;
    private ControlsPaneController controlsPaneController;
    private MediaPaneController mediaPaneController;
    private MediaDescriptionEditorPane2Controller mediaDescriptionEditorPaneController;
    private BulkEditorPaneController bulkEditorPaneController;
    private AncillaryDataPaneController ancillaryDataPaneController;
//    private RectLabelStageController rectLabelStageController;
//    private IFXStageController ifxStageController;
    private final AnnotationViewController annotationViewController;
    private final MachineLearningStageController machineLearningStageController;

    private static final String masterPaneKey =  "master-split-pane";
    private static final String topPaneKey = "top-split-pane";
    private static final String bottomPaneKey = "bottom-split-pane";
    private static final String concurrentStatusKey = "concurrent-status-key";
    private static final String jsonStatusKey = "json-association-key";
    private static final String currentGroupKey = "current-group-key";
    private final Logger log = LoggerFactory.getLogger(getClass());


    public AppPaneController(UIToolBox toolBox) {
        this.toolBox = toolBox;
        selectMediaDialog = new SelectMediaDialog(toolBox,
                toolBox.getI18nBundle());
        selectMediaDialog.getDialogPane()
                .getStylesheets()
                .addAll(toolBox.getStylesheets());
        selectMediaDialog.initOwner(toolBox.getPrimaryStage());

        realTimeDialog = new OpenRealTimeDialog(toolBox.getI18nBundle());
        realTimeDialog.getDialogPane()
                .getStylesheets()
                .addAll(toolBox.getStylesheets());
        realTimeDialog.initOwner(toolBox.getPrimaryStage());

        tapeDialog = new OpenTapeDialog(toolBox.getI18nBundle());
        tapeDialog.getDialogPane()
                .getStylesheets()
                .addAll(toolBox.getStylesheets());
        tapeDialog.initOwner(toolBox.getPrimaryStage());

//        annotationTableController = new AnnotationTableController(toolBox);
        annotationTableController = new JXAnnotationTableController(toolBox);
        preferencesDialogController = new PreferencesDialogController(toolBox);
        imageViewController = new ImageViewController2(toolBox);
        controlsPaneController = new ControlsPaneController(toolBox);
        mediaPaneController = MediaPaneController.newInstance();
        mediaDescriptionEditorPaneController = new MediaDescriptionEditorPane2Controller(toolBox);
        bulkEditorPaneController = BulkEditorPaneController.newInstance(toolBox,
                toolBox.getData().getAnnotations(),
                toolBox.getData().getSelectedAnnotations(),
                toolBox.getEventBus());
        ancillaryDataPaneController = new AncillaryDataPaneController(toolBox);
        machineLearningStageController = new MachineLearningStageController(toolBox);
        annotationViewController = new AnnotationViewController(toolBox);
//        ifxStageController = new IFXStageController(toolBox);
//        ifxStageController.getStage().setOnCloseRequest(evt -> ifxStageController.setVisible(false));
//        rectLabelStageController = new RectLabelStageController(toolBox);
//        rectLabelStageController.getStage().setOnCloseRequest(evt -> rectLabelStageController.hide());

        toolBox.getEventBus()
                .toObserverable()
                .ofType(ReloadServicesMsg.class)
                .subscribe(msg -> reload());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            saveDividerPositions(masterPaneKey, getMasterPane());
            saveDividerPositions(bottomPaneKey, getBottomPane());
            saveDividerPositions(topPaneKey, getTopPane());
            saveBooleanPreference(concurrentStatusKey, getShowConcurrentCheckBox().isSelected());
            saveBooleanPreference(jsonStatusKey, getShowJsonAssociationsCheckBox().isSelected());
            saveBooleanPreference(currentGroupKey, getShowCurrentGroupOnlyCheckBox().isSelected());
        }));


    }

    public BorderPane getRoot() {
        if (root == null) {
            root = new BorderPane(getMasterPane());
            root.setTop(getToolBar());
            root.setBottom(getUtilityPane());
        }
        return root;
    }

    private void saveDividerPositions(String name, SplitPane pane) {
        // Pref path is UserNode/Class/name/0 (or 1 or 2)
        Preferences p0 = Preferences.userNodeForPackage(getClass());
        Preferences p1 = p0.node(name);
        double[] pos = pane.getDividerPositions();
        for (int i = 0; i < pos.length; i++) {
            p1.putDouble(i+ "", pos[i]);
        }
    }

    private void saveBooleanPreference(String name, Boolean value) {
        Preferences p0 = Preferences.userNodeForPackage(getClass());
        Preferences p1 = p0.node(name);
        p1.putBoolean("is-selected", value);
    }

    private Boolean loadBooleanPreference(String name) {
        Preferences p0 = Preferences.userNodeForPackage(getClass());
        Preferences p1 = p0.node(name);
        return p1.getBoolean("is-selected", false);
    }

    private void loadDividerPositions(String name, SplitPane pane) {
        Preferences p0 = Preferences.userNodeForPackage(getClass());
        Preferences p1 = p0.node(name);
        double[] positions = pane.getDividerPositions();
        for (int i = 0; i < positions.length; i++) {
            try {
                double v = p1.getDouble(i + "", positions[i]);
                pane.setDividerPosition(i, v);
            }
            catch (Exception e) {
                // TODO log it
            }
        }
    }


    public SplitPane getMasterPane() {
        if (masterPane == null) {
            masterPane = new SplitPane(getTopPane(), getBottomPane());
            masterPane.setOrientation(Orientation.VERTICAL);
            loadDividerPositions(masterPaneKey, masterPane);
        }
        return masterPane;

    }

    public SplitPane getTopPane() {
        if (topPane == null) {
            var swingNode = new SwingNode();
            SwingUtilities.invokeLater(() -> {
                var table = annotationTableController.getTable();
                var scrollPane = new JScrollPane(table);
                table.setFillsViewportHeight(true);
                swingNode.setContent(scrollPane);
                table.revalidate();
                table.packAll();

            });
            topPane = new SplitPane(swingNode,
                   getTabPane());
            imageViewController.getImageView()
                    .fitWidthProperty()
                    .bind(Bindings.subtract(1, topPane.getDividers().
                            get(0).positionProperty()).multiply(topPane.widthProperty()));
            loadDividerPositions(topPaneKey, topPane);
        }
        return topPane;
    }

    public SplitPane getBottomPane() {
        if (bottomPane == null) {
            ConceptButtonPanesController panesController = new ConceptButtonPanesController(toolBox);
            bottomPane = new SplitPane(controlsPaneController.getRoot(),
                    panesController.getRoot());
            bottomPane.setOrientation(Orientation.VERTICAL);
            loadDividerPositions(bottomPaneKey, bottomPane);
        }
        return bottomPane;
    }

    public TabPane getTabPane() {
        if (tabPane == null) {
            tabPane = new JFXTabPane();

            io.reactivex.rxjava3.core.Observable<Object> observable = toolBox.getEventBus().toObserverable();

            Tab imageTab = new Tab("Images", imageViewController.getRoot());
            imageTab.setClosable(false);
            observable.ofType(AnnotationsSelectedEvent.class)
                    .subscribe(evt -> {
                        Collection<Annotation> annotations = evt.get();
                        if (annotations == null || annotations.size() != 1) {
                            imageViewController.setAnnotation(null);
                        }
                        else {
                            imageViewController.setAnnotation(annotations.iterator().next());
                        }
                    });

            Tab bulkEditTab = new Tab("Bulk Editor", new ScrollPane(bulkEditorPaneController.getRoot()));
            bulkEditTab.setClosable(false);

            Tab mediaTab = new Tab("Media", new ScrollPane(mediaPaneController.getRoot()));
            mediaTab.setClosable(false);
            observable.ofType(AnnotationsSelectedEvent.class)
                    .subscribe(e -> showMediaOfSelectedRow(e.get()));

            Tab dataTab = new Tab("Data", ancillaryDataPaneController.getRoot());
            dataTab.setClosable(false);
            observable.ofType(AnnotationsSelectedEvent.class)
                    .subscribe(e -> showAncillaryData(e.get()));

            SearchTreePaneController treeController = new SearchTreePaneController(toolBox,
                    toolBox.getI18nBundle());
            observable.ofType(ShowConceptInTreeViewMsg.class)
                    .subscribe(msg -> treeController.setSearchText(msg.getName()));
            Tab treeTab = new Tab("Knowledgebase", treeController.getRoot());
            treeTab.setClosable(false);

            Tab mediaDescriptionTab = new Tab("Media Info", new ScrollPane(mediaDescriptionEditorPaneController.getRoot()));
            mediaDescriptionTab.setClosable(false);
            // mediaDescriptionTab is updated by showMediaOfSelectedRow


            tabPane.getTabs().addAll(imageTab, bulkEditTab, treeTab, dataTab, mediaTab, mediaDescriptionTab);

        }
        return tabPane;
    }


    public ToolBar getToolBar() {
        if (toolBar == null) {
            ResourceBundle bundle = toolBox.getI18nBundle();

            Text openIcon = Icons.VIDEO_LIBRARY.standardSize();
            Button openButton = new JFXButton();
            openButton.setGraphic(openIcon);
            openButton.setOnAction(e -> getOpenPopOver().show(openButton));
            openButton.setTooltip(new Tooltip(bundle.getString("apppane.toolbar.button.open")));
            JFXUtilities.attractAttention(openButton);
            toolBox.getData()
                    .mediaProperty()
                    .addListener((obs, oldv, newv) -> {
                        if (newv == null) {
                            JFXUtilities.attractAttention(openButton);
                        }
                        else {
                            JFXUtilities.removeAttention(openButton);
                        }
                    });

            Text closeIcon = Icons.CLOSE.standardSize();
            Button closeButton = new JFXButton();
            closeButton.setGraphic(closeIcon);
            closeButton.setTooltip(new Tooltip(bundle.getString("apppane.toolbar.button.close")));
            closeButton.setOnAction(e -> {
                if (toolBox.getData().getMedia() != null) {
                    toolBox.getEventBus().send(new MediaChangedEvent(AppPaneController.this, null));
                }
            });

            Text undoIcon = Icons.UNDO.standardSize();
            Button undoButton = new JFXButton();
            undoButton.setGraphic(undoIcon);
            undoButton.setOnAction(e -> toolBox.getEventBus().send(new UndoMsg()));
            undoButton.setTooltip(new Tooltip(bundle.getString("apppane.toolbar.button.undo")));

            Text redoIcon = Icons.REDO.standardSize();
            Button redoButton = new JFXButton();
            redoButton.setGraphic(redoIcon);
            redoButton.setOnAction(e -> toolBox.getEventBus().send(new RedoMsg()));
            redoButton.setTooltip(new Tooltip(bundle.getString("apppane.toolbar.button.redo")));

            Text refreshIcon = Icons.CACHED.standardSize();
            Button refreshButton = new JFXButton();
            refreshButton.setGraphic(refreshIcon);
            refreshButton.setOnAction(e -> toolBox.getEventBus().send(new ReloadServicesMsg()));
            refreshButton.setTooltip(new Tooltip(bundle.getString("apppane.toolbar.button.refresh")));

            Text prefsIcon = Icons.SETTINGS.standardSize();
            Button prefsButton = new JFXButton();
            prefsButton.setGraphic(prefsIcon);
            prefsButton.setOnAction(e -> preferencesDialogController.show());
            prefsButton.setTooltip(new Tooltip(bundle.getString("apppane.toolbar.button.prefs")));

            Text usersIcon = Icons.PERSON_ADD.standardSize();
            Button createUserButton = new JFXButton();
            createUserButton.setGraphic(usersIcon);
            createUserButton.setOnAction(e -> {
                CreateUserDialog dialog = new CreateUserDialog(toolBox);
                Optional<User> user = dialog.showAndWait();
                user.ifPresent(u -> toolBox.getEventBus()
                        .send(new UserChangedEvent(u)));
            });
            createUserButton.setTooltip(new Tooltip(bundle.getString("apppane.toolbar.button.user")));

            Text deploymentIcon = Icons.GRID_ON.standardSize();
            Button showDeploymentButton = new JFXButton();
            showDeploymentButton.setGraphic(deploymentIcon);
            showDeploymentButton.setOnAction(e -> annotationViewController.show());
            showDeploymentButton.setTooltip(new Tooltip(bundle.getString("apppane.toolbar.button.deployment")));

            Text rectLabelIcon = Icons.PICTURE_IN_PICTURE.standardSize();
            Button rectLabelButton = new JFXButton();
            rectLabelButton.setGraphic(rectLabelIcon);
//            rectLabelButton.setOnAction(e -> rectLabelStageController.show());
//            rectLabelButton.setOnAction(e -> ifxStageController.setVisible(true));
            rectLabelButton.setTooltip(new Tooltip(bundle.getString("apppane.toolbar.button.rectlabel")));
            rectLabelButton.setDisable(true);

            Label videoLabel = new Label(toolBox.getI18nBundle().getString("apppane.label.media"));
            Label mediaLabel = new Label();
            toolBox.getEventBus()
                    .toObserverable()
                    .ofType(MediaChangedEvent.class)
                    .subscribe(e -> {
                        Platform.runLater(() -> {
                            mediaLabel.setText(null);
                            Media media = e.get();
                            if (media != null) {
                                String uri = media.getUri().toString();
                                int i = uri.lastIndexOf("/");
                                if (i < 0) {
                                    i = 0;
                                }
                                else {
                                    i = i + 1;
                                }
                                uri = uri.substring(i);
                                mediaLabel.setText(media.getVideoName() + " [" + uri + "]");
                            }
                        });
                    });


            toolBar = new ToolBar(openButton,
                    closeButton,
                    undoButton,
                    redoButton,
                    refreshButton,
                    showDeploymentButton,
                    rectLabelButton,
                    prefsButton,
                    createUserButton,
                    new Label(bundle.getString("apppane.label.user")),
                    getUsersComboBox(),
                    videoLabel,
                    mediaLabel);
        }
        return toolBar;
    }

    public PopOver getOpenPopOver() {
        if (openPopOver == null) {

            ResourceBundle i18n = toolBox.getI18nBundle();

            Text remoteIcon = Icons.VIDEO_LIBRARY.standardSize();
            Button remoteButton = new JFXButton(null, remoteIcon);
            remoteButton.setTooltip(new Tooltip(i18n.getString("apppane.button.open.remote")));
            remoteButton.setOnAction(e -> {
                Optional<Media> media = selectMediaDialog.showAndWait();
                media.ifPresent(m -> toolBox.getEventBus().send(new MediaChangedEvent(null, m)));
            });

            Text localIcon = Icons.FOLDER.standardSize();
            Button localButton = new JFXButton(null, localIcon);
            localButton.setTooltip(new Tooltip(i18n.getString("apppane.button.open.local")));
            FileBrowsingDecorator decorator = new FileBrowsingDecorator(toolBox);
            localButton.setOnAction(e ->
                decorator.apply(AppPaneController.this.getRoot().getScene().getWindow()));

            // Tape is no longer used at MBARI. Remove this on 2022-01-21
//            Text tapeIcon = Icons.LIVE_TV.standardSize();
//            Button tapeButton = new JFXButton(null, tapeIcon);
//            tapeButton.setTooltip(new Tooltip(i18n.getString("apppane.button.open.tape")));
//            tapeButton.setOnAction(e -> {
//                tapeDialog.refresh();
//                Optional<org.mbari.vars.ui.mediaplayers.vcr.MediaParams> opt = tapeDialog.showAndWait();
//                opt.ifPresent(mediaParams -> {
//                    OpenTapeService ots = new OpenTapeService(toolBox);
//                    ots.open(mediaParams);
//                });
//            });

            Text realtimeIcon = Icons.DIRECTIONS_BOAT.standardSize();
            Button realtimeButton = new JFXButton(null, realtimeIcon);
            realtimeButton.setTooltip(new Tooltip(i18n.getString("apppane.button.open.realtime")));
            realtimeButton.setOnAction(e -> {
                realTimeDialog.refresh();
                Optional<MediaParams> opt = realTimeDialog.showAndWait();
                opt.ifPresent(mediaParams -> {
                    OpenRealTimeService rts = new OpenRealTimeService(toolBox);
                    rts.open(mediaParams);
                });

            });

            VBox vbox = new VBox(remoteButton, localButton, realtimeButton);

            openPopOver = new PopOver(vbox);

        }
        return openPopOver;
    }

    public ComboBox<String> getUsersComboBox() {
        if (usersComboBox == null) {

            usersComboBox = new JFXComboBox<>();
            new FilteredComboBoxDecorator<>(usersComboBox, FilteredComboBoxDecorator.CONTAINS_CHARS_IN_ORDER);
            Comparator<String> sorter = Comparator.comparing(String::toString, String.CASE_INSENSITIVE_ORDER);

            // Listen to UserAddedEvent and add it to the combobox
            EventBus eventBus = toolBox.getEventBus();
            eventBus.toObserverable()
                    .ofType(UserAddedEvent.class)
                    .subscribe(event -> {
                        Platform.runLater(() -> {
                            User user = event.get();
                            // M3-53 fix. Copy to editable arraylist first
                            List<String> newItems = new ArrayList<>(usersComboBox.getItems());
                            newItems.add(user.getUsername());
                            Collections.sort(newItems, sorter);
                            ObservableList<String> items = FXCollections.observableList(newItems);
                            usersComboBox.setItems(items);
                            usersComboBox.getSelectionModel().select(user.getUsername());
                        });
                    });

            // When a username is selected send a change event
            usersComboBox.getSelectionModel()
                    .selectedItemProperty()
                    .addListener((obs, oldv, newv) -> {
                if (newv != null) {
                    toolBox.getServices()
                            .getUserService()
                            .findAllUsers()
                            .thenAccept(users -> {
                                Optional<User> opt = users.stream()
                                        .filter(u -> u.getUsername().equals(newv))
                                        .findFirst();
                                opt.ifPresent(user -> eventBus.send(new UserChangedEvent(user)));
                            });
                }
            });

            loadUsers();

            // Listen for new services event and update users after service is changed.
            toolBox.getEventBus()
                    .toObserverable()
                    .ofType(ReloadServicesMsg.class)
                    .subscribe(evt -> loadUsers());

        }
        return usersComboBox;
    }

    /**
     * Populate the user combobox and select the user from the OS
     */
    private void loadUsers() {
        Comparator<String> sorter = Comparator.comparing(String::toString, String.CASE_INSENSITIVE_ORDER);
        usersComboBox.setItems(FXCollections.observableList(new ArrayList<>()));
        toolBox.getServices()
                .getUserService()
                .findAllUsers()
                .thenAccept(users -> {
                    List<String> usernames = users.stream()
                            .map(User::getUsername)
                            .sorted(sorter)
                            .collect(Collectors.toList());
                    Platform.runLater(() -> {
                        usersComboBox.setItems(FXCollections.observableList(usernames));
                        String defaultUser = System.getProperty("user.name");
                        if (usernames.contains(defaultUser)) {
                            usersComboBox.getSelectionModel().select(defaultUser);
                        }
                    });
                });
    }

    public StatusBar getUtilityPane() {
        if (utilityPane == null) {
            utilityPane = new StatusBar();
            utilityPane.setText(null);

            // --- Listen for progress bar notifications
            Observable<Object> observable = toolBox.getEventBus().toObserverable();
            observable.ofType(ShowProgress.class)
                    .subscribe(s -> Platform.runLater(() -> utilityPane.setProgress(0.00001)));
            observable.ofType(SetProgress.class)
                    .subscribe(s -> Platform.runLater(() -> utilityPane.setProgress(s.getProgress())));
            observable.ofType(HideProgress.class)
                    .subscribe(s ->
                        Platform.runLater(() -> utilityPane.setProgress(0.0)));
            observable.ofType(SetStatusBarMsg.class)
                    .subscribe(s -> Platform.runLater(() -> utilityPane.setText(s.getMsg())));

            // --- Configure group control
            Label groupLabel = new Label(toolBox.getI18nBundle()
                    .getString("apppane.statusbar.label.group"));
            groupLabel.getStyleClass().add("utility-label");
//            ComboBox<String> groupCombobox = new JFXComboBox<>();
            groupCombobox.setEditable(true);
//            toolBox.getServices()
//                    .getAnnotationService()
//                    .findGroups()
//                    .thenAccept(groups ->
//                        Platform.runLater(() ->
//                            groupCombobox.getItems().addAll(groups)));
            groupCombobox.getSelectionModel()
                    .selectedItemProperty()
                    .addListener((obs, oldv, newv) -> toolBox.getData().setGroup(newv));
            groupCombobox.valueProperty().addListener((obs, oldv, newv) ->
                    toolBox.getData().setGroup(newv));

            // Set default value if defined in config.
            try {
                String defaultGroup = toolBox.getConfig()
                        .getString("app.defaults.group");
                groupCombobox.getSelectionModel().select(defaultGroup);
            }
            catch (Exception e) {
                log.info("Default group is not defined in configuration files. (app.defaults.group)");
            }

            // --- Configure activity controls
            Label activityLabel = new Label(toolBox.getI18nBundle()
                    .getString("apppane.statusbar.label.activity"));
            activityLabel.getStyleClass().add("utility-label");
//            ComboBox<String> activityCombobox = new JFXComboBox<>();
            activityCombobox.setEditable(true);

//            toolBox.getServices()
//                    .getAnnotationService()
//                    .findActivities()
//                    .thenAccept(activities -> {
//                        Platform.runLater(() -> {
//                            activityCombobox.getItems().addAll(activities);
//                        });
//                    });
            activityCombobox.getSelectionModel()
                    .selectedItemProperty()
                    .addListener((obs, oldv, newv) -> toolBox.getData().setActivity(newv));
            activityCombobox.valueProperty().addListener((obs, oldv, newv) ->
                    toolBox.getData().setActivity(newv));

            // Set default value if defined in config.
            try {
                String defaultActivity = toolBox.getConfig()
                        .getString("app.defaults.activity");
                activityCombobox.getSelectionModel().select(defaultActivity);
            }
            catch (Exception e) {
                log.info("Default activity is not defined in configuration files. (app.defaults.activity)");
            }

            // --- Configure concurrent controls


            CheckBox checkBox = getShowConcurrentCheckBox();
            CheckBox checkBox1 = getShowJsonAssociationsCheckBox();
            CheckBox checkBox2 = getShowCurrentGroupOnlyCheckBox();

            Pane spacer0 = new Pane();
            spacer0.setPrefSize(20, 5);
            Pane spacer1 = new Pane();
            spacer1.setPrefSize(20, 5);
            Pane spacer2 = new Pane();
            spacer2.setPrefSize(20, 5);
            Pane spacer3 = new Pane();
            spacer3.setPrefSize(20, 5);

            utilityPane.getLeftItems()
                    .addAll(groupLabel,
                            groupCombobox,
                            spacer0,
                            activityLabel,
                            activityCombobox,
                            spacer1,
                            checkBox,
                            spacer2,
                            checkBox1,
                            spacer3,
                            checkBox2);

            reload();
        }
        return utilityPane;
    }

    private void reload() {
        toolBox.getServices()
                .getAnnotationService()
                .findActivities()
                .thenAccept(activities ->
                        Platform.runLater(() ->
                                activityCombobox.getItems().addAll(activities)));
        toolBox.getServices()
                .getAnnotationService()
                .findGroups()
                .thenAccept(groups ->
                        Platform.runLater(() ->
                                groupCombobox.getItems().addAll(groups)));
    }

    private CheckBox getShowConcurrentCheckBox() {
        if (showConcurrentCheckBox == null) {
            showConcurrentCheckBox = new JFXCheckBox(toolBox.getI18nBundle()
                    .getString("apppane.statusbar.label.concurrent"));
            showConcurrentCheckBox.getStyleClass().add("utility-label");
            Boolean isSelected = loadBooleanPreference(concurrentStatusKey);
            showConcurrentCheckBox.setSelected(isSelected);
            toolBox.getData().setShowConcurrentAnnotations(isSelected);
            showConcurrentCheckBox.selectedProperty()
                    .addListener((obs, oldv, newv) ->
                            toolBox.getEventBus().send(new ShowConcurrentAnnotationsMsg(newv)));
            // When the media is changed unselect the check box
//            toolBox.getData()
//                    .mediaProperty()
//                    .addListener((obs, oldv, newv) -> showConcurrentCheckBox.setSelected(false));
        }
        return showConcurrentCheckBox;
    }

    private CheckBox getShowJsonAssociationsCheckBox() {
        if (showJsonAssociationsCheckBox == null) {
            showJsonAssociationsCheckBox = new JFXCheckBox(toolBox.getI18nBundle()
                    .getString("apppane.statusbar.label.json"));
            showJsonAssociationsCheckBox.getStyleClass().add("utility-label");
            Boolean isSelected = loadBooleanPreference(jsonStatusKey);
            showJsonAssociationsCheckBox.setSelected(isSelected);
            showJsonAssociationsCheckBox.selectedProperty()
                    .addListener((obs, oldv, newv) ->
                            toolBox.getEventBus().send(new ShowJsonAssociationsMsg(newv)));
            toolBox.getEventBus().send(new ShowJsonAssociationsMsg(isSelected));
        }
        return showJsonAssociationsCheckBox;
    }

    private CheckBox getShowCurrentGroupOnlyCheckBox() {
        if (showCurrentGroupOnlyCheckBox == null) {
            showCurrentGroupOnlyCheckBox = new JFXCheckBox(toolBox.getI18nBundle()
                    .getString("apppane.statusbar.label.current"));
            showCurrentGroupOnlyCheckBox.getStyleClass().add("utility-label");
            Boolean isSelected = loadBooleanPreference(currentGroupKey);
            showCurrentGroupOnlyCheckBox.setSelected(isSelected);
            showCurrentGroupOnlyCheckBox.selectedProperty()
                    .addListener((obs, oldv, newv) ->
                            toolBox.getEventBus().send(new ShowCurrentGroupOnlyMsg(newv)));
            toolBox.getEventBus().send(new ShowCurrentGroupOnlyMsg(isSelected));

        }
        return showCurrentGroupOnlyCheckBox;
    }

    public JXAnnotationTableController getAnnotationTableController() {
        return annotationTableController;
    }

    private void showMediaOfSelectedRow(Collection<Annotation> annotations) {
        final AnnotationService annotationService = toolBox.getServices().getAnnotationService();
        if (annotations == null || annotations.size() != 1) {
            mediaPaneController.setMedia(null, annotationService);
            mediaDescriptionEditorPaneController.setMedia(null);
        }
        else {
            Annotation annotation = annotations.iterator().next();
            Media media = toolBox.getData().getMedia();
            if (media != null &&
                    annotation.getVideoReferenceUuid().equals(media.getVideoReferenceUuid())) {
                mediaPaneController.setMedia(media,
                        annotationService);
                mediaDescriptionEditorPaneController.setMedia(media);
            }
            else {
                toolBox.getServices()
                        .getMediaService()
                        .findByUuid(annotation.getVideoReferenceUuid())
                        .thenAccept(m -> {
                            mediaPaneController.setMedia(m, annotationService);
                            mediaDescriptionEditorPaneController.setMedia(m);
                        });
            }
        }
    }

    private void showAncillaryData(Collection<Annotation> annotations) {
        if (annotations == null || annotations.size() != 1) {
            ancillaryDataPaneController.setAncillaryData(null);
        }
        else {
            Annotation first = annotations.iterator().next();
            ancillaryDataPaneController.setAncillaryData(first.getObservationUuid());
        }
    }
}
