package org.mbari.vars.annotation.ui.mediaplayers.sharktopoda2;

import io.reactivex.rxjava3.disposables.Disposable;
import org.mbari.vars.annotation.etc.jdk.Loggers;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.commands.CreateAnnotationAtIndexWithAssociationCmd;
import org.mbari.vars.annotation.ui.commands.DeleteAssociationsCmd;
import org.mbari.vars.annotation.ui.commands.UpdateAssociationCmd;
import org.mbari.vars.annotation.ui.events.AnnotationsSelectedEvent;
import org.mbari.vars.annotation.ui.mediaplayers.sharktopoda.Constants;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.remote.control.RemoteControl;
import org.mbari.vcr4j.remote.control.commands.localization.*;
import org.mbari.vcr4j.remote.player.RxControlRequestHandler;

import java.util.*;
import java.util.stream.Collectors;

public class IncomingController {

    private final RemoteControl remoteControl;
    private final RxControlRequestHandler requestHandler;
    private final UIToolBox toolBox;
    private final List<Disposable> disposables = new ArrayList<>();
    private final SharktopodaState sharktopodaState;
    private static final Loggers log = new Loggers(IncomingController.class);
    private final Comparator<LocalizedAnnotation> comparator = Comparator.comparing(a -> a.association().getUuid());

    // Keep the constructor package-private so this controller does not expose a
    // type from the vcr4j.remote module through its public API.
    IncomingController(UIToolBox toolBox,
                      RemoteControl remoteControl,
                      SharktopodaState sharktopodaState) {
        this.remoteControl = remoteControl;
        this.requestHandler = remoteControl.getRequestHandler();
        this.toolBox = toolBox;
        this.sharktopodaState = sharktopodaState;
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
            log.atInfo().log("An attempt was made to add a localization, but no media is currently open");
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
                    IncomingController.class);
                    //LocalizationController.EVENT_SOURCE); #170
            toolBox.getEventBus().send(cmd);

            // Two issues here. The first is that we need to get the localization UUID to match the
            // corresponding association UUID.So we just create a new localization and delete the original
            // The second issue is that the localization was being removed too quickly. So we send the
            // remove after the new one has been created (Yes order is important here.).
            // https://github.com/mbari-org/vars-annotation/issues/170
            // https://github.com/mbari-org/vars-annotation/issues/174
            remoteControl.getVideoIO().send(new RemoveLocalizationsCmd(remoteControl.getVideoIO().getUuid(),
                    List.of(loc.getUuid())));

        });

    }

    private void handleRemove(List<UUID> removed) {
        Media media = toolBox.getData().getMedia();
        if (media == null) {
            log.atInfo().log("An attempt was made to remove a localization, but no media is currently open");
            return;
        }
        var matches = searchByUuid(removed);
        var map = matches.stream()
                .map(LocalizationPair::localizedAnnotation)
                .collect(Collectors.toMap(LocalizedAnnotation::association, a -> a.annotation().getObservationUuid()));
        if (!map.isEmpty()) {
            // Tagged so OutgoingController doesn't echo the change back to Sharktopoda
            var cmd = new DeleteAssociationsCmd(map, Constants.LOCALIZATION_EVENT_SOURCE);
            toolBox.getEventBus().send(cmd);
        }
    }

    private void handleUpdate(List<Localization> updated) {
        Media media = toolBox.getData().getMedia();
        if (media == null) {
            log.atInfo().log("An attempt was made to update a localization, but no media is currently open");
            return;
        }
        var matches = search(updated);
        for (var m : matches) {
            var existing = m.localizedAnnotation();
            var provided = LocalizedAnnotation.from(m.localization());
            // Tagged so OutgoingController doesn't echo the change back to Sharktopoda. Echoing
            // it interrupts the user while they're still moving/resizing the box.
            var cmd = new UpdateAssociationCmd(existing.annotation().getObservationUuid(),
                    existing.association(),
                    provided.association(),
                    Constants.LOCALIZATION_EVENT_SOURCE);
            toolBox.getEventBus().send(cmd);
        }
    }

    private void handleSelect(List<UUID> selected) {
        Media media = toolBox.getData().getMedia();
        if (media == null) {
            log.atInfo().log("An attempt was made to update a localization, but no media is currently open");
            return;
        }
        var matches = searchByUuid(selected);
        var matchingLocalizationUuids = matches.stream()
                .map(lp -> lp.localization().getUuid())
                .toList();
        sharktopodaState.setSelectedLocalizations(matchingLocalizationUuids);

        var selectedAnnotations = matches.stream()
                .map(LocalizationPair::localizedAnnotation)
                .map(LocalizedAnnotation::annotation)
                .toList();
        // Tag the event so OutgoingController does not echo the selection back to
        // Sharktopoda. The echo can expand to a different set of localizations (an
        // annotation may have several bounding boxes) and the apps re-select at each other.
        var cmd = new AnnotationsSelectedEvent(Constants.LOCALIZATION_EVENT_SOURCE, selectedAnnotations);
        toolBox.getEventBus().send(cmd);
    }

    private List<LocalizationPair> searchByUuid(List<UUID> uuids) {
        var mockLocalizations = uuids.stream()
                .map(uuid -> new Localization(uuid, null, null, null, 0, 0, 0, 0, null))
                .toList();
        return search(mockLocalizations);
    }

    private List<LocalizationPair> search(List<Localization> xs) {
        var existingBoxes = AnnotationSnapshots.snapshot(toolBox)
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
