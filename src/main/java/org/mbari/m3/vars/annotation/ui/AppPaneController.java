package org.mbari.m3.vars.annotation.ui;

import com.anchorage.docks.node.DockNode;
import com.anchorage.docks.stations.DockStation;
import com.anchorage.system.AnchorageSystem;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import de.jensd.fx.glyphs.GlyphsFactory;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import io.reactivex.Observable;
import io.reactivex.rxjavafx.observables.JavaFxObservable;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.StatusBar;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.events.AnnotationsSelectedEvent;
import org.mbari.m3.vars.annotation.messages.*;
import org.mbari.m3.vars.annotation.events.MediaChangedEvent;
import org.mbari.m3.vars.annotation.events.UserAddedEvent;
import org.mbari.m3.vars.annotation.events.UserChangedEvent;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.model.User;
import org.mbari.m3.vars.annotation.services.UserService;
import org.mbari.m3.vars.annotation.ui.annotable.AnnotationTableController;
import org.mbari.m3.vars.annotation.ui.cbpanel.ConceptButtonPanesController;
import org.mbari.m3.vars.annotation.ui.concepttree.SearchTreePaneController;
import org.mbari.m3.vars.annotation.ui.mediadialog.SelectMediaDialog;
import org.mbari.m3.vars.annotation.ui.prefs.PreferencesDialogController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-07-26T14:37:00
 */
public class AppPaneController {

    private BorderPane root;
    private DockStation dockStation;
    private final AnnotationTableController annotationTableController;
    private ToolBar toolBar;
    private final UIToolBox toolBox;
    private ComboBox<String> usersComboBox;
    private PopOver openPopOver;
    private StatusBar utilityPane;
    private final ImageViewController imageViewController;
    private final PreferencesDialogController preferencesDialogController;
    //private final FramegrabPaneController framegrabPaneController;
    private final SelectMediaDialog selectMediaDialog;



    public AppPaneController(UIToolBox toolBox) {
        this.toolBox = toolBox;
        selectMediaDialog = new SelectMediaDialog(toolBox.getServices().getMediaService(),
                toolBox.getI18nBundle());
        selectMediaDialog.getDialogPane().getStylesheets().addAll(toolBox.getStylesheets());
        annotationTableController = new AnnotationTableController(toolBox);
        preferencesDialogController = new PreferencesDialogController(toolBox);
        imageViewController = new ImageViewController();

        //framegrabPaneController = FramegrabPaneController.newInstance();
    }

    public BorderPane getRoot() {
        if (root == null) {
            root = new BorderPane(getDockStation());
            root.setTop(getToolBar());
            root.setBottom(getUtilityPane());
        }
        return root;
    }

    public DockStation getDockStation() {
        if (dockStation == null) {
            dockStation = AnchorageSystem.createStation();
            DockNode annotationNode = AnchorageSystem.createDock("Annotations", annotationTableController.getTableView());
            annotationNode.closeableProperty().set(false);
            annotationNode.dock(dockStation, DockNode.DockPosition.CENTER);

            SearchTreePaneController treeController = new SearchTreePaneController(toolBox.getServices().getConceptService(),
                    toolBox.getI18nBundle());
            toolBox.getEventBus()
                    .toObserverable()
                    .ofType(ShowConceptInTreeViewMsg.class)
                    .subscribe(msg -> treeController.setSearchText(msg.getName()));
            DockNode treeNode = AnchorageSystem.createDock("Knowledgebase", treeController.getRoot());
            treeNode.closeableProperty().set(false);
            treeNode.setPrefSize(400, 400);
            treeNode.dock(dockStation, DockNode.DockPosition.RIGHT);

            DockNode imageViewNode = AnchorageSystem.createDock("Images", imageViewController.getRoot());
            imageViewNode.closeableProperty().set(false);
            imageViewNode.dock(treeNode, DockNode.DockPosition.CENTER);
            toolBox.getEventBus()
                    .toObserverable()
                    .ofType(AnnotationsSelectedEvent.class)
                    .subscribe(evt -> {
                        Collection<Annotation> annotations = evt.get();
                        if (annotations == null || annotations.size() != 1) {
                            imageViewController.setAnnotation(null);
                        }
                        else {
                            imageViewController.setAnnotation(annotations.iterator().next());
                        }
                    });

            ConceptButtonPanesController panesController = new ConceptButtonPanesController(toolBox);
            Pane pane = panesController.getRoot();
            pane.setPrefSize(800, 250);
            DockNode cbNode = AnchorageSystem.createDock("Quick Buttons", pane);
            cbNode.closeableProperty().set(false);
            cbNode.maximizableProperty().set(false);
            cbNode.dock(dockStation, DockNode.DockPosition.BOTTOM);

        }
        return dockStation;
    }

    public ToolBar getToolBar() {
        if (toolBar == null) {
            ResourceBundle bundle = toolBox.getI18nBundle();
            GlyphsFactory gf = MaterialIconFactory.get();

            Text openIcon = gf.createIcon(MaterialIcon.VIDEO_LIBRARY, "30px");
            Button openButton = new JFXButton();
            openButton.setGraphic(openIcon);
            openButton.setOnAction(e -> getOpenPopOver().show(openButton));
            openButton.setTooltip(new Tooltip(bundle.getString("apppane.toolbar.button.open")));

            Text undoIcon = gf.createIcon(MaterialIcon.UNDO, "30px");
            Button undoButton = new JFXButton();
            undoButton.setGraphic(undoIcon);
            undoButton.setOnAction(e -> toolBox.getEventBus().send(new UndoMsg()));
            undoButton.setTooltip(new Tooltip(bundle.getString("apppane.toolbar.button.undo")));

            Text redoIcon = gf.createIcon(MaterialIcon.REDO, "30px");
            Button redoButton = new JFXButton();
            redoButton.setGraphic(redoIcon);
            redoButton.setOnAction(e -> toolBox.getEventBus().send(new RedoMsg()));
            redoButton.setTooltip(new Tooltip(bundle.getString("apppane.toolbar.button.redo")));

            Text refreshIcon = gf.createIcon(MaterialIcon.CACHED, "30px");
            Button refreshButton = new JFXButton();
            refreshButton.setGraphic(refreshIcon);
            refreshButton.setOnAction(e -> toolBox.getEventBus().send(new ClearCacheMsg()));
            refreshButton.setTooltip(new Tooltip(bundle.getString("apppane.toolbar.button.refresh")));

            Text prefsIcon = gf.createIcon(MaterialIcon.SETTINGS, "30px");
            Button prefsButton = new JFXButton();
            prefsButton.setGraphic(prefsIcon);
            prefsButton.setOnAction(e -> preferencesDialogController.show());
            prefsButton.setTooltip(new Tooltip(bundle.getString("apppane.toolbar.button.prefs")));

            Label videoLabel = new Label(toolBox.getI18nBundle().getString("apppane.label.media"));
            Label mediaLabel = new Label();
            toolBox.getEventBus()
                    .toObserverable()
                    .ofType(MediaChangedEvent.class)
                    .subscribe(e -> {
                        mediaLabel.setText(null);
                        Media media = e.get();
                        if (media != null) {
                            String uri = media.getUri().toString();
                            if (uri.length() > 70) {
                                int n = uri.length();
                                uri = uri.substring(n - 70, n);
                            }
                            mediaLabel.setText(media.getVideoName() + " [" + uri + "]");
                        }
                    });


            toolBar = new ToolBar(openButton,
                    undoButton,
                    redoButton,
                    refreshButton,
                    prefsButton,
                    new Label(bundle.getString("apppane.label.user")),
                    getUsersComboBox(),
                    videoLabel,
                    mediaLabel);
        }
        return toolBar;
    }

    public PopOver getOpenPopOver() {
        if (openPopOver == null) {

            GlyphsFactory gf = MaterialIconFactory.get();
            ResourceBundle i18n = toolBox.getI18nBundle();

            Text remoteIcon = gf.createIcon(MaterialIcon.VIDEO_LIBRARY, "30px");
            Button remoteButton = new JFXButton(null, remoteIcon);
            remoteButton.setTooltip(new Tooltip(i18n.getString("apppane.button.open.remote")));
            remoteButton.setOnAction(e -> {
                Optional<Media> media = selectMediaDialog.showAndWait();
                media.ifPresent(m -> toolBox.getEventBus().send(new MediaChangedEvent(null, m)));
            });

            Text localIcon = gf.createIcon(MaterialIcon.QUEUE, "30px");
            Button localButton = new JFXButton(null, localIcon);
            localButton.setTooltip(new Tooltip(i18n.getString("apppane.button.open.local")));

            Text tapeIcon = gf.createIcon(MaterialIcon.PERM_MEDIA, "30px");
            Button tapeButton = new JFXButton(null, tapeIcon);
            tapeButton.setTooltip(new Tooltip(i18n.getString("apppane.button.open.tape")));

            Text realtimeIcon = gf.createIcon(MaterialIcon.TIMER, "30px");
            Button realtimeButton = new JFXButton(null, realtimeIcon);
            realtimeButton.setTooltip(new Tooltip(i18n.getString("apppane.button.open.realtime")));

            VBox vbox = new VBox(remoteButton, localButton, tapeButton, realtimeButton);

            openPopOver = new PopOver(vbox);

        }
        return openPopOver;
    }

    public ComboBox<String> getUsersComboBox() {
        if (usersComboBox == null) {

            UserService userService = toolBox.getServices().getUserService();
            usersComboBox = new JFXComboBox<>();
            Comparator<String> sorter = Comparator.comparing(String::toString, String.CASE_INSENSITIVE_ORDER);

            // Listen to UserAddedEvent and add it to the combobox
            EventBus eventBus = toolBox.getEventBus();
            eventBus.toObserverable()
                    .ofType(UserAddedEvent.class)
                    .subscribe(event -> {
                        User user = event.get();
                        usersComboBox.getItems().add(user.getUsername());
                        FXCollections.sort(usersComboBox.getItems(), sorter);
                        usersComboBox.getSelectionModel().select(user.getUsername());
                    });

            // When a username is selected send a change event
            JavaFxObservable.valuesOf(usersComboBox.getSelectionModel().selectedItemProperty())
                    .subscribe(s -> {
                        userService.findAllUsers()
                                .thenAccept(users -> {
                                    Optional<User> opt = users.stream()
                                            .filter(u -> u.getUsername().equals(s))
                                            .findFirst();
                                    opt.ifPresent(user -> eventBus.send(new UserChangedEvent(user)));
                                });
                    });

            // Populate the combobox and select the user form the OS
            userService.findAllUsers()
                    .thenAccept(users -> {
                        List<String> usernames = users.stream()
                                .map(User::getUsername)
                                .sorted(sorter)
                                .collect(Collectors.toList());
                        usersComboBox.setItems(FXCollections.observableList(usernames));
                        String defaultUser = System.getProperty("user.name");
                        usersComboBox.getSelectionModel().select(defaultUser);
                    });

        }
        return usersComboBox;
    }

    public StatusBar getUtilityPane() {
        if (utilityPane == null) {
            utilityPane = new StatusBar();

            // Listen for progress bar notifications
            Observable<Object> observable = toolBox.getEventBus().toObserverable();
            observable.ofType(ShowProgress.class)
                    .subscribe(s -> utilityPane.setProgress(0.00001));
            observable.ofType(SetProgress.class)
                    .subscribe(s -> utilityPane.setProgress(s.getProgress()));
            observable.ofType(HideProgress.class)
                    .subscribe(s -> utilityPane.setProgress(0.0));

        }
        return utilityPane;
    }


}
