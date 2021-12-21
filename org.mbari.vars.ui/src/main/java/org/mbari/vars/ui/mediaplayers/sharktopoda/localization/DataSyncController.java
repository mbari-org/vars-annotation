package org.mbari.vars.ui.mediaplayers.sharktopoda.localization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vcr4j.sharktopoda.client.gson.DurationConverter;
import org.mbari.vcr4j.sharktopoda.client.localization.IO;
import org.mbari.vcr4j.sharktopoda.client.localization.Localization;

import java.io.Closeable;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class DataSyncController implements Closeable  {

    private final UIToolBox toolBox;
    private final IO io;
    private record AnnoWithBox(Annotation annotation, Association association) {}
    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .registerTypeAdapter(Duration.class, new DurationConverter())
            .create();

    private final ObservableList<LocalizedAnnotation> localizedAnnotations = FXCollections.observableArrayList();
    private final Comparator<LocalizedAnnotation> abComparator = Comparator.comparing(a -> a.association().getUuid());

    public DataSyncController(UIToolBox toolBox, IO io) {
        this.toolBox = toolBox;
        this.io = io;
    }

    private void init() {
        toolBox.getData()
                .getAnnotations()
                .addListener((ListChangeListener<Annotation>) c -> {
                    if (c.wasAdded()) {
                        handleAnnotationsAdd(c.getAddedSubList());
                    }
                    else if (c.wasRemoved()) {
                        handleAnnotationsRemove(c.getRemoved());
                    }
                });

        io.getController()
                .getLocalizations()
                .addListener((ListChangeListener<? super Localization>) c -> {
                    if (c.wasAdded()) {
                        handleLocalizationsAdd(c.getAddedSubList());
                    }
                    else if (c.wasRemoved()) {
                        handleLocalizationsRemove(c.getRemoved());
                    }
                });
    }

//    private void internalSyncAdd(AnnoWithBox ab) {
//        var idx = Collections.binarySearch(localizedAnnotations, ab, abComparator);
//        if (idx >= 0) {
//            var match = localizedAnnotations.get(idx);
//            if
//        }
//        if (localizedAnnotations.)
//    }

    private void handleAnnotationsAdd(List<? extends Annotation> xs) {
        xs.stream()
                .map(LocalizedAnnotation::from)
                .flatMap(Collection::stream)
                .forEach(this::handleAnnotationAdd);

    }

    private Optional<LocalizedAnnotation> searchLocal(LocalizedAnnotation a) {
        var searchList = localizedAnnotations.subList(0, localizedAnnotations.size());
        return LocalizedAnnotation.search(searchList, a.association().getUuid());
    }

    private Optional<Localization> searchRemote(LocalizedAnnotation a) {
        return io.getController()
                .getLocalizations()
                .stream()
                .filter(ass -> ass.getLocalizationUuid().equals(a.association().getUuid()))
                .findFirst();

    }

    private void handleAnnotationAdd(LocalizedAnnotation a) {
        var searchList = localizedAnnotations.subList(0, localizedAnnotations.size());
        var opt = LocalizedAnnotation.search(searchList, a.association().getUuid());
        if (opt.isEmpty()) {

        }
    }

    private void handleAnnotationsRemove(List<? extends Annotation> xs) {

    }

    private void handleLocalizationsAdd(List<? extends Localization> xs) {

    }

    private void handleLocalizationsRemove(List<? extends Localization> xs) {

    }

    private void exists(AnnoWithBox ab) {

    }

    private void exists(Localization loc) {

    }



    private Association toAssociation(Localization x) {
        BoundingBox bb = new BoundingBox(x.getX(), x.getY(), x.getWidth(), x.getHeight(), "VARS Annotation");
        String json = gson.toJson(bb);

        return new Association(BoundingBox.LINK_NAME,
                Association.VALUE_SELF,
                json,
                "application/json",
                x.getLocalizationUuid());
    }

    public List<Localization> toLocalizations(Annotation a) {
        List<Localization> xs = new ArrayList<>();
        for (Association ass: a.getAssociations()) {
            if (ass.getLinkName().equalsIgnoreCase("bounding box") &&
                    ass.getMimeType().equalsIgnoreCase("application/json")) {
                BoundingBox box = gson.fromJson(ass.getLinkValue(), BoundingBox.class);
                Localization x = new Localization(a.getConcept(),
                        a.getElapsedTime(),
                        ass.getUuid(),
                        a.getVideoReferenceUuid(),
                        box.getX(),
                        box.getY(),
                        box.getWidth(),
                        box.getHeight(),
                        a.getDuration(),
                        a.getObservationUuid());
                xs.add(x);
            }
        }
        return xs;
    }

    @Override
    public void close() {

    }
}
