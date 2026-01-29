package org.mbari.vars.annotation.ui.javafx.deployeditor;


import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.control.StatusBar;
import org.mbari.vars.annotation.etc.rxjava.EventBus;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.events.AnnotationsAddedEvent;
import org.mbari.vars.annotation.ui.events.AnnotationsChangedEvent;
import org.mbari.vars.annotation.ui.events.AnnotationsRemovedEvent;
import org.mbari.vars.annotation.ui.events.MediaChangedEvent;
import org.mbari.vars.annotation.ui.messages.HideProgress;
import org.mbari.vars.annotation.ui.messages.SetProgress;
import org.mbari.vars.annotation.ui.messages.SetStatusBarMsg;
import org.mbari.vars.annotation.ui.messages.ShowProgress;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;
import org.mbari.vars.annotation.ui.services.MultiAnnotationDecorator;
import org.mbari.vars.annotation.ui.javafx.BulkEditorPaneController;
import org.mbari.vars.annotation.ui.util.JFXUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Brian Schlining
 * @since 2018-04-05T17:02:00
 */
public class AnnotationViewController {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final UIToolBox toolBox;
    private Stage stage;
    private AnnotationTableController tableController;
    private BulkEditorPaneController bulkEditorPaneController;
    private StatusBar statusBar;
    private boolean disabled = true;
    private final List<Disposable> disposables = new ArrayList<>();
    private ObservableList<Annotation> annotations;
    private ObservableList<Annotation> selectedAnnotations;
    private final EventBus eventBus = new EventBus();

    public AnnotationViewController(UIToolBox toolBox) {
        this.toolBox = toolBox;
        annotations = getTableController().getTableView().getItems();
        selectedAnnotations = getTableController().getTableView()
                .getSelectionModel()
                .getSelectedItems();

        EventBus mainEventBus = toolBox.getEventBus();
        Observable<Object> observable = mainEventBus.toObserverable();
        observable.ofType(AnnotationsAddedEvent.class)
                .subscribe(eventBus::send);

        observable.ofType(AnnotationsRemovedEvent.class)
                .subscribe(eventBus::send);

        observable.ofType(AnnotationsChangedEvent.class)
                .subscribe(eventBus::send);

    }

    public void show() {
        getStage().show();
        setDisabled(false);
    }

    public void hide() {
        getStage().close();
        setDisabled(true);
    }

    public Stage getStage() {
        if (stage == null) {
            stage = new Stage();
            BorderPane pane = new BorderPane(getTableController().getTableView());
            pane.setBottom(getStatusBar());
            VBox bulkEditorPane = getBulkEditorPaneController().getRoot();
            pane.setTop(bulkEditorPane);
            BorderPane.setAlignment(bulkEditorPane, Pos.CENTER);
            Scene scene = new Scene(pane);
            scene.getStylesheets()
                    .addAll(toolBox.getStylesheets());
            stage.setScene(scene);

            // --- Set stage size from user preferences
            final Class clazz = getClass();
            JFXUtilities.loadStageSize(stage, clazz);

            stage.setOnCloseRequest(evt -> {
                JFXUtilities.saveStageSize(stage, clazz);
                hide();
            });

            stage.focusedProperty().addListener((obs, oldv, newv) -> {
                JFXUtilities.saveStageSize(stage, clazz);
            });


        }
        return stage;
    }

    public StatusBar getStatusBar() {
        if (statusBar == null) {
            statusBar = new StatusBar();
            statusBar.setText(null);

            // --- Listen for progress bar notifications
            Observable<Object> observable = eventBus.toObserverable();
            observable.ofType(ShowProgress.class)
                    .subscribe(s -> Platform.runLater(() -> statusBar.setProgress(0.00001)));
            observable.ofType(SetProgress.class)
                    .subscribe(s -> Platform.runLater(() -> statusBar.setProgress(s.getProgress())));
            observable.ofType(HideProgress.class)
                    .subscribe(s -> {
                        log.warn("HideProgress received");
                        Platform.runLater(() -> {
                            statusBar.setText(null);
                            statusBar.setProgress(0.0);
                        });
                    });
            observable.ofType(SetStatusBarMsg.class)
                    .subscribe(s -> Platform.runLater(() -> statusBar.setText(s.getMsg())));
        }
        return statusBar;
    }

    public AnnotationTableController getTableController() {
        if (tableController == null) {
            tableController = new AnnotationTableController(toolBox, eventBus);
            Runtime.getRuntime()
                    .addShutdownHook(new Thread(() -> tableController.savePreferences()));
        }
        return tableController;
    }

    public BulkEditorPaneController getBulkEditorPaneController() {
        if (bulkEditorPaneController == null) {

            bulkEditorPaneController = BulkEditorPaneController.newInstance(toolBox,
                    annotations, selectedAnnotations, eventBus);
        }
        return bulkEditorPaneController;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        getTableController().setDisabled(disabled);
        if (this.disabled != disabled) {

            if (disabled) {
                disable();
            }
            else {
                enable();
            }
        }
        this.disabled = disabled;
    }

    private void loadMedia(Media media) {
        TableView<Annotation> tableView = getTableController().getTableView();
        ObservableList<Annotation> items = tableView.getItems();
        items.clear();
        if (media != null) {
            tableView.setDisable(true);
            eventBus.send(new ShowProgress());
            eventBus.send(new SetStatusBarMsg("Loading all annotation for " +
                    media.getVideoSequenceName()));


            MultiAnnotationDecorator decorator = new MultiAnnotationDecorator(toolBox,
                    eventBus);
            Observable<List<Annotation>> observable = decorator.loadMultiAnnotations(media);
            observable.subscribe(items::addAll,
                    ex -> {},
                    () -> {
                        tableView.setDisable(false);
                        getBulkEditorPaneController().refresh();
                    });

//            CombinedMediaAnnotationDecorator decorator = new CombinedMediaAnnotationDecorator(toolBox);
//            decorator.findAllAnnotationsInDeployment(media.getVideoSequenceName())
//                    .thenAccept(as -> {
//                        log.debug("---- FOUND " + as.size() + " annotations");
//                        items.addAll(as);
//                        tableView.setDisable(false);
//                        getBulkEditorPaneController().refresh();
//                        eventBus.send(new HideProgress());
//                    });
        }
    }

    private void enable() {
        EventBus eventBus = toolBox.getEventBus();
        Observable<Object> observable = eventBus.toObserverable();

        Disposable disposable0 = observable.ofType(MediaChangedEvent.class)
                .subscribe(e -> loadMedia(e.get()));
        disposables.add(disposable0);

        Media media = toolBox.getData().getMedia();
        loadMedia(media);

    }

    private void disable() {
        disposables.forEach(Disposable::dispose);
        loadMedia(null);
    }


}

