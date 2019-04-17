package org.mbari.m3.vars.annotation.ui.deployeditor;

import com.google.common.collect.Lists;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.UIToolBox;

import org.mbari.m3.vars.annotation.commands.*;
import org.mbari.m3.vars.annotation.events.*;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.services.MediaService;
import org.mbari.m3.vars.annotation.ui.shared.AnnotationTableViewFactory;
import org.mbari.m3.vars.annotation.util.AsyncUtils;
import org.mbari.m3.vars.annotation.util.JFXUtilities;
import org.mbari.m3.vars.annotation.util.ListUtils;

import javax.annotation.Nonnull;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2018-04-05T15:00:00
 */
public class AnnotationTableController {

    private TableView<Annotation> tableView;
    private final ResourceBundle i18n;
    private final Map<UUID, URI> videoReferenceUriMap  = new ConcurrentHashMap<>();
    private final UIToolBox toolBox;
    private final List<Disposable> disposables = new ArrayList<>();
    private final EventBus eventBus;
    private boolean disabled = true;

    public AnnotationTableController(@Nonnull UIToolBox toolBox,
                                     @Nonnull EventBus eventBus) {
        this.toolBox = toolBox;
        this.i18n = toolBox.getI18nBundle();
        this.eventBus = eventBus;

        Observable<Object> observable = eventBus.toObserverable();

        // Forward this tables annotation mutating events to main event bus
        EventBus mainEventBus = toolBox.getEventBus();
        ArrayList<Class<? extends Command>> commandsToForward = Lists.newArrayList(ChangeGroupCmd.class,
                ChangeActivityCmd.class,
                //MoveAnnotationsCmd.class,
                MoveAnnotationsAndImagesCmd.class,
                ChangeConceptCmd.class,
                DeleteAssociationsCmd.class);
        for (Class<? extends Command> clazz : commandsToForward) {
            observable.ofType(clazz)
                    .subscribe(mainEventBus::send);
        }

        // Load the column visibility and width
        loadPreferences();

        // After annotations are added do lookup of video URI for video URI column
        getTableView().getItems()
                .addListener((InvalidationListener) obs -> updateVideoReferenceUris());

    }

    private void select(Collection<Annotation> annos) {
        JFXUtilities.runOnFXThread(() -> {
            TableView.TableViewSelectionModel<Annotation> selectionModel = getTableView().getSelectionModel();
            selectionModel.clearSelection();
            annos.forEach(selectionModel::select);
            selectionModel.getSelectedIndices()
                    .stream()
                    .min(Comparator.naturalOrder())
                    .ifPresent(i -> getTableView().scrollTo(i));
        });
    }

    private void updateVideoReferenceUris() {
        ObservableList<Annotation> items = getTableView().getItems();
        List<UUID> uuids = items.stream()
                .map(Annotation::getVideoReferenceUuid)
                .distinct()
                .collect(Collectors.toList());

        Set<UUID> existingUuids = videoReferenceUriMap.keySet();

        List<UUID> newUuids = new ArrayList<>(uuids);
        newUuids.removeAll(existingUuids);

        if (newUuids.size() > 0) {
            videoReferenceUriMap.clear();
            MediaService mediaService = toolBox.getServices().getMediaService();
            AsyncUtils.collectAll(uuids, mediaService::findByUuid)
                    .thenAccept(medias ->
                            medias.forEach(m -> videoReferenceUriMap.put(m.getVideoReferenceUuid(), m.getUri())))
                    .thenAccept(v -> getTableView().refresh());
        }
    }


    private void loadPreferences() {
        // Load the column visibility and width
        Preferences prefs = Preferences.userNodeForPackage(getClass());
        Preferences columnPrefs = prefs.node("table-columns");
        getTableView().getColumns()
                .forEach(tc -> {
                    Preferences p = columnPrefs.node(tc.getId());
                    String s = p.get("visible", "true");
                    boolean isVisible = s.equals("true");
                    String w = p.get("width", "100");
                    double width = Double.parseDouble(w);
                    JFXUtilities.runOnFXThread(() -> {
                        tc.setVisible(isVisible);
                        tc.setPrefWidth(width);
                    });
                });

    }

    public void savePreferences() {
        Preferences prefs = Preferences.userNodeForPackage(getClass());
        Preferences columnPrefs = prefs.node("table-columns");
        getTableView().getColumns()
                .forEach(tc -> {
                    Preferences p = columnPrefs.node(tc.getId());
                    p.put("visible", "false");
                    p.put("width", tc.getWidth() + "");
                });
        getTableView().getVisibleLeafColumns()
                .forEach(tc -> {
                    Preferences p = columnPrefs.node(tc.getId());
                    p.put("visible", "true");
                });
    }

    public TableView<Annotation> getTableView() {
        if (tableView == null) {
            tableView = AnnotationTableViewFactory.newTableView(i18n);

            TableColumn<Annotation, URI> vruCol
                    = new TableColumn<>(i18n.getString("annotable.col.videoreference"));
            vruCol.setCellValueFactory(param -> {
                URI uri = null;
                if (param.getValue() != null) {
                    uri = videoReferenceUriMap.get(param.getValue().getVideoReferenceUuid());
                }
                return new SimpleObjectProperty<>(uri);
            });
            vruCol.setId("videoReferenceUuid");

            // TODO get column order from preferences
            tableView.getColumns().add(vruCol);

        }
        return tableView;
    }

    private void enable() {
        Observable<Object> observable = eventBus.toObserverable();

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
                        ObservableList<Annotation> items = getTableView().getItems();
                        List<Annotation> intersection = ListUtils.intersection(annotations, items);
                        for (Annotation a : intersection) {
                            int idx = items.indexOf(a);
                            items.remove(idx);
                            items.add(idx, a);
                        }
                        tableView.refresh();
                        tableView.sort();
                        eventBus.send(new AnnotationsSelectedEvent(intersection));

                    });
                });
        disposables.add(disposable3);

        Disposable disposable4 = observable.ofType(AnnotationsSelectedEvent.class)
                .subscribe(e -> select(e.get()));
        disposables.add(disposable4);
    }

    private void disable() {
        disposables.forEach(Disposable::dispose);
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
}
