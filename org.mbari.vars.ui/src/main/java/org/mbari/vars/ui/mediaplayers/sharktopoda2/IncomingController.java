package org.mbari.vars.ui.mediaplayers.sharktopoda2;

import io.reactivex.rxjava3.disposables.Disposable;
import org.mbari.vars.services.model.Media;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.commands.CreateAnnotationAtIndexWithAssociationCmd;
import org.mbari.vars.ui.commands.DeleteAssociationsCmd;
import org.mbari.vars.ui.commands.UpdateAssociationCmd;
import org.mbari.vars.ui.events.AnnotationsSelectedEvent;
import org.mbari.vars.ui.mediaplayers.sharktopoda.localization.LocalizationController;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.remote.control.commands.localization.*;
import org.mbari.vcr4j.remote.player.RxControlRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class IncomingController {

    private final RxControlRequestHandler requestHandler;
    private final UIToolBox toolBox;
    private final List<Disposable> disposables = new ArrayList<>();
    private static final Logger log = LoggerFactory.getLogger(IncomingController.class);
    private final Comparator<LocalizedAnnotation> comparator = Comparator.comparing(a -> a.association().getUuid());

    public IncomingController(UIToolBox toolBox, RxControlRequestHandler requestHandler) {
        this.requestHandler = requestHandler;
        this.toolBox = toolBox;
        init();
    }

    private void init() {
        var bus = requestHandler.getLocalizationsCmdObservable();

        var d = bus.ofType(AddLocalizationsCmd.class)
                .subscribe(evt -> handleAdd(evt.getValue().getLocalizations()));
        disposables.add(d);

        d = bus.ofType(RemoveLocalizationsCmd.class)
                .subscribe(evt -> handleRemove(evt.getValue().getLocalizations()));
        disposables.add(d);

        d = bus.ofType(UpdateLocalizationsCmd.class)
                .subscribe(evt -> handleUpdate(evt.getValue().getLocalizations()));
        disposables.add(d);

        d = bus.ofType(SelectLocalizationsCmd.class)
                .subscribe(evt -> handleSelect(evt.getValue().getLocalizations()));
        disposables.add(d);

        d = bus.subscribe(evt -> log.atDebug().log(() -> "Incoming from Sharktopoda: " + evt));
        disposables.add(d);

    }

    private void handleAdd(List<Localization> added) {
        Media media = toolBox.getData().getMedia();
        if (media == null) {
            log.info("An attempt was made to add a localization, but no media is currently open");
            return;
        }
        added.forEach(loc -> {
            var localizedAnnotation = LocalizedAnnotation.from(loc);
            var annotation = localizedAnnotation.annotation();
            var association = localizedAnnotation.association();

            var videoIndex = new VideoIndex(localizedAnnotation.annotation().getElapsedTime());
            var cmd = new CreateAnnotationAtIndexWithAssociationCmd(videoIndex,
                    annotation.getConcept(),
                    association,
                    LocalizationController.EVENT_SOURCE);
            toolBox.getEventBus().send(cmd);
        });

    }

    private void handleRemove(List<UUID> removed) {
        Media media = toolBox.getData().getMedia();
        if (media == null) {
            log.info("An attempt was made to remove a localization, but no media is currently open");
            return;
        }
        var matches = searchByUuid(removed);
        var map = matches.stream()
                .map(LocalizationPair::localizedAnnotation)
                .collect(Collectors.toMap(LocalizedAnnotation::association, a -> a.annotation().getObservationUuid()));
        var cmd = new DeleteAssociationsCmd(map);
        toolBox.getEventBus().send(cmd);
    }

    private void handleUpdate(List<Localization> updated) {
        Media media = toolBox.getData().getMedia();
        if (media == null) {
            log.info("An attempt was made to update a localization, but no media is currently open");
            return;
        }
        var matches = search(updated);
        for (var m : matches) {
            var existing = m.localizedAnnotation();
            var provided = LocalizedAnnotation.from(m.localization());
            var cmd = new UpdateAssociationCmd(existing.annotation().getObservationUuid(),
                    existing.association(),
                    provided.association());
            toolBox.getEventBus().send(cmd);
        }
    }

    private void handleSelect(List<UUID> selected) {
        Media media = toolBox.getData().getMedia();
        if (media == null) {
            log.info("An attempt was made to update a localization, but no media is currently open");
            return;
        }
        var matches = searchByUuid(selected);
        var selectedAnnotations = matches.stream()
                .map(LocalizationPair::localizedAnnotation)
                .map(LocalizedAnnotation::annotation)
                .toList();
        var cmd = new AnnotationsSelectedEvent(this, selectedAnnotations);
        toolBox.getEventBus().send(cmd);
    }

    private List<LocalizationPair> searchByUuid(List<UUID> uuids) {
        var mockLocalizations = uuids.stream()
                .map(uuid -> new Localization(uuid, null, null, null, 0, 0, 0, 0, null))
                .toList();
        return search(mockLocalizations);
    }

    private List<LocalizationPair> search(List<Localization> xs) {
        var existingBoxes = toolBox.getData()
                .getAnnotations()
                .stream()
                .flatMap(anno -> LocalizedAnnotation.from(anno).stream())
                .sorted(comparator)
                .toList();

        var matches = new ArrayList<LocalizationPair>();
        // Loop over provided Localizations
        for (var provided : xs) {
            var box = LocalizedAnnotation.from(provided);
            // Search for existing annotation's UUID that matches the localizations UUID
            var idx = Collections.binarySearch(existingBoxes, box, comparator);
            if (idx >= 0) {
                var existing = existingBoxes.get(idx);
                matches.add(new LocalizationPair(existing, provided));
            }
        }
        return matches;
    }

    public void close() {
        disposables.forEach(Disposable::dispose);
        disposables.clear();
    }
}
