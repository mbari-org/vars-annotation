package org.mbari.m3.vars.annotation.ui.deployeditor;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.events.AnnotationsAddedEvent;
import org.mbari.m3.vars.annotation.events.AnnotationsChangedEvent;
import org.mbari.m3.vars.annotation.events.AnnotationsRemovedEvent;
import org.mbari.m3.vars.annotation.events.MediaChangedEvent;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.services.CombinedMediaAnnotationDecorator;
import org.mbari.m3.vars.annotation.util.JFXUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
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
    private boolean disabled = true;
    private final List<Disposable> disposables = new ArrayList<>();

    public AnnotationViewController(UIToolBox toolBox) {
        this.toolBox = toolBox;
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
            Scene scene = new Scene(pane);
            scene.getStylesheets()
                    .addAll(toolBox.getStylesheets());
            stage.setScene(scene);
            stage.setOnCloseRequest(evt -> {
                stage.close();
                setDisabled(true);
            });
        }
        return stage;
    }

    public AnnotationTableController getTableController() {
        if (tableController == null) {
            tableController = new AnnotationTableController(toolBox);
            Runtime.getRuntime()
                    .addShutdownHook(new Thread(() -> tableController.savePreferences()));
        }
        return tableController;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
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
            CombinedMediaAnnotationDecorator decorator = new CombinedMediaAnnotationDecorator(toolBox);
            decorator.findAllAnnotationsInDeployment(media.getVideoSequenceName())
                    .thenAccept(as -> {
                        log.debug("---- FOUND " + as.size() + " annotations");
                        items.addAll(as);
                        tableView.setDisable(false);
                    });
        }
    }

    private void enable() {
        EventBus eventBus = toolBox.getEventBus();
        Observable<Object> observable = eventBus.toObserverable();
        TableView<Annotation> tableView = getTableController().getTableView();

        Disposable disposable0 = observable.ofType(MediaChangedEvent.class)
                .subscribe(e -> loadMedia(e.get()));
        disposables.add(disposable0);

        Disposable disposable1 = observable.ofType(AnnotationsAddedEvent.class)
                .subscribe(e -> JFXUtilities.runOnFXThread(() -> {
                    tableView.getItems().addAll(e.get());
                    tableView.sort();
                }));
        disposables.add(disposable1);

        Disposable disposable2 = observable.ofType(AnnotationsRemovedEvent.class)
                .subscribe(e -> JFXUtilities.runOnFXThread(() ->
                        tableView.getItems().removeAll(e.get())));
        disposables.add(disposable2);

        Disposable disposable3 = observable.ofType(AnnotationsChangedEvent.class)
                .subscribe(e -> {
                    JFXUtilities.runOnFXThread(() -> {
                        Collection<Annotation> annotations = e.get();
                        ObservableList<Annotation> items = tableView.getItems();
                        for (Annotation a : annotations) {
                            int idx = items.indexOf(a);
                            items.remove(idx);
                            items.add(idx, a);
                        }
                        tableView.refresh();
                        tableView.sort();
                    });
                });
        disposables.add(disposable3);

        Media media = toolBox.getData().getMedia();
        loadMedia(media);

    }

    private void disable() {
        disposables.forEach(Disposable::dispose);
        loadMedia(null);
    }


}

