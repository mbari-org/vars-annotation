package org.mbari.vars.ui.mediaplayers.sharktopoda.localization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import mbarix4j.util.Tuple2;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;
import org.mbari.vars.services.model.BoundingBox;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.commands.CreateAnnotationAtIndexWithAssociationCmd;
import org.mbari.vars.ui.commands.UpdateAssociationCmd;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.sharktopoda.client.gson.DurationConverter;
import org.mbari.vcr4j.sharktopoda.client.localization.IO;
import org.mbari.vcr4j.sharktopoda.client.localization.Localization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class DataSyncController implements Closeable  {

    private final UIToolBox toolBox;
    private final IO io;
    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .registerTypeAdapter(Duration.class, new DurationConverter())
            .create();
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ObservableList<LocalizedAnnotation> localizedAnnotations = FXCollections.observableArrayList();
    private final Comparator<LocalizedAnnotation> abComparator = Comparator.comparing(a -> a.association().getUuid());

    public DataSyncController(UIToolBox toolBox, IO io) {
        this.toolBox = toolBox;
        this.io = io;
        init();
    }

    private void init() {
        toolBox.getData()
                .getAnnotations()
                .addListener((ListChangeListener<Annotation>) c -> {
                    while(c.next()) {
                        if (c.wasAdded()) {
                            handleAddAnnotations(c.getAddedSubList());
                        } else if (c.wasRemoved()) {
                            handleRemoveAnnotations(c.getRemoved());
                        }
                    }
                });

        io.getController()
                .getLocalizations()
                .addListener((ListChangeListener<? super Localization>) c -> {
                    while(c.next()) {
                        if (c.wasAdded()) {
                            handleAddLocalizations(c.getAddedSubList());
                        } else if (c.wasRemoved()) {
                            handleRemoveLocalizations(c.getRemoved());
                        }
                    }
                });

        var selected = toolBox.getData().getSelectedAnnotations();
        selected
                .addListener((InvalidationListener) c -> {
                    var s = selected.stream()
                            .map(LocalizedAnnotation::from)
                            .flatMap(Collection::stream)
                            .map(LocalizedAnnotation::localization)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());

                    io.getSelectionController().select(s, true);
                });
    }


    private Tuple2<Integer, Optional<LocalizedAnnotation>> searchLocal(LocalizedAnnotation a) {
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

    private void handleAddAnnotations(List<? extends Annotation> xs) {
        xs.stream()
                .map(LocalizedAnnotation::from)
                .flatMap(Collection::stream)
                .forEach(this::addAnnotation);

    }

    private void addAnnotation(LocalizedAnnotation a) {
        var searchList = localizedAnnotations.subList(0, localizedAnnotations.size());
        var match = LocalizedAnnotation.search(searchList, a.association().getUuid());
        if (match.getB().isEmpty()) {
            // New annotation not in LocalizedAnnotation List
            var idx = -match.getA() - 1;
            localizedAnnotations.add(idx, a);
            var remoteOpt = searchRemote(a);
            if (remoteOpt.isEmpty() || !areLocalizationsSame(a.localization().get(), remoteOpt.get())) {
                io.getController().addLocalization(a.localization().get());
            }
        }
        else {
            // An annotation with the matching assocation uuid already exist in the LocalizatedAnnotation List
            var aloc = a.localization().get();
            var bloc = match.getB().get().localization().get();
            if (areLocalizationsSame(aloc, bloc)) {
                // Do nothing
            }
            else {
                // New Localized annotation. Added it to the list.
                localizedAnnotations.set(match.getA(), a);
                var remoteOpt = searchRemote(a);
                if (remoteOpt.isEmpty() || !areLocalizationsSame(aloc, remoteOpt.get())) {
                    io.getController().addLocalization(a.localization().get());
                }
            }
        }
    }

    private void handleRemoveAnnotations(List<? extends Annotation> xs) {
        xs.stream()
                .map(LocalizedAnnotation::from)
                .flatMap(Collection::stream)
                .forEach(this::removeAnnotation);
    }

    private void removeAnnotation(LocalizedAnnotation a) {
        var searchList = localizedAnnotations.subList(0, localizedAnnotations.size());
        var localMatch = LocalizedAnnotation.search(searchList, a.association().getUuid());
        var remote = searchRemote(a);
        if (localMatch.getB().isEmpty()) {
            // Nothing to do here
        }
        else {
            log.atDebug().log("Removing localized annotation: " + a.annotation().getConcept());
            localizedAnnotations.remove(localMatch.getA().intValue());
            if (remote.isPresent()) {
                io.getController().removeLocalization(a.association().getUuid());
            }
        }
    }

    private void handleAddLocalizations(List<? extends Localization> xs) {
        xs.forEach(this::addLocalization);
    }

    private void addLocalization(Localization loc) {
        var la = LocalizedAnnotation.from(loc);
        var match = searchLocal(la);
        if (match.getB().isEmpty()) {
            // Send new message to create new annotation and association
            var videoIndex = new VideoIndex(loc.getElapsedTime());
            log.atDebug().log("Creating new annotation from localization");
            var cmd = new CreateAnnotationAtIndexWithAssociationCmd(videoIndex,
                    loc.getConcept(), la.association());
            toolBox.getEventBus().send(cmd);
        }
        else if (!areLocalizationsSame(loc, match.getB().get().localization().get())){
            // send new message to updated existing annotation and association
            // Are
            var annotation = match.getB().get();
            var oldAssociation = annotation.association();
            var newAssociation = la.association();
            log.atDebug().log("Updating existing annotation from localization");
            var cmd = new UpdateAssociationCmd(annotation.annotation().getObservationUuid(),
                    oldAssociation, newAssociation);
            toolBox.getEventBus().send(cmd);
        }
    }

    private void handleRemoveLocalizations(List<? extends Localization> xs) {
        xs.forEach(this::removeLocalization);
    }

    private void removeLocalization(Localization loc) {
        log.atDebug().log("Called remove localization but I'm doing nothing:  " + loc.getConcept());
//        var la = LocalizedAnnotation.from(loc);
//        var match = searchLocal(la);
//        if (match.getB().isEmpty()) {
//            // Do nothing
//        }
//        else {
//            if (!areLocalizationsSame(loc, match.getB().get().localization().get())) {
//                var cmd = new Up
//            }
//        }
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

    private static boolean areLocalizationsSame(Localization a, Localization b) {
        // We don't need to compare all the fields. Only concept, time, and coords
        // are normally changed
        return   a.getLocalizationUuid().equals(b.getLocalizationUuid()) &&
                a.getConcept().equals(b.getConcept()) &&
                a.getX().equals(b.getX()) &&
                a.getY().equals(b.getY()) &&
                a.getHeight().equals(b.getHeight()) &&
                a.getWidth().equals(b.getWidth()) &&
                a.getElapsedTime().equals(b.getElapsedTime()) &&
                Objects.equals(a.getDuration(), b.getDuration());

    }
}
