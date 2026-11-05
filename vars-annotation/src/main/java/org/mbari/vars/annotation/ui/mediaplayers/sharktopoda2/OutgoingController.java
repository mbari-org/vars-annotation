package org.mbari.vars.annotation.ui.mediaplayers.sharktopoda2;

import io.reactivex.rxjava3.disposables.Disposable;
import org.mbari.vars.annotation.etc.rxjava.EventBus;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.events.*;
import org.mbari.vars.annotation.ui.mediaplayers.sharktopoda.Constants;
import org.mbari.vars.annotation.ui.messages.SeekedToAnnotationMsg;
import org.mbari.vcr4j.remote.control.RVideoIO;
import org.mbari.vcr4j.remote.control.commands.localization.*;
import org.mbari.vars.annotation.etc.jdk.Loggers;

import java.util.*;
import java.util.stream.Collectors;

class OutgoingController {

    private static final Loggers log = new Loggers(OutgoingController.class);

    private final UIToolBox toolBox;
    private final RVideoIO io;
    private final List<Disposable> disposables = new ArrayList<>();
    private final SharktopodaState sharktopodaState;
    private final SentLocalizations sentLocalizations = new SentLocalizations();
    private volatile boolean openDone = false;
    private enum Action {
        Add, Remove, Select, Update
    }

    OutgoingController(UIToolBox toolBox,
                      RVideoIO io,
                      SharktopodaState sharktopodaState) {
        this.toolBox = toolBox;
        this.io = io;
        this.sharktopodaState = sharktopodaState;
        init(toolBox.getEventBus());
    }

    private void init(EventBus eventBus) {

        var observable = eventBus.toObserverable();

        disposables.add(observable
                .ofType(AnnotationsAddedEvent.class)
                .filter(evt -> evt.getEventSource() != Constants.LOCALIZATION_EVENT_SOURCE)
                .filter(evt -> !evt.get().isEmpty())
                .subscribe(evt -> handle(evt.get(), Action.Add)));

        disposables.add(observable
                .ofType(AnnotationsRemovedEvent.class)
                .filter(evt -> evt.getEventSource() != Constants.LOCALIZATION_EVENT_SOURCE)
                .filter(evt -> !evt.get().isEmpty())
                .subscribe(evt -> handle(evt.get(), Action.Remove)));

        disposables.add(observable
                .ofType(AnnotationsChangedEvent.class)
                .filter(evt -> evt.getEventSource() != Constants.LOCALIZATION_EVENT_SOURCE)
                .filter(evt -> !evt.get().isEmpty())
                .subscribe(evt -> handle(evt.get(), Action.Update)));

        disposables.add(observable
                .ofType(AnnotationsSelectedEvent.class)
                .filter(evt -> evt.getEventSource() != Constants.LOCALIZATION_EVENT_SOURCE)
                .subscribe(evt -> handle(evt.get(), Action.Select)));

        disposables.add(observable
                .ofType(SeekedToAnnotationMsg.class)
                .subscribe(msg -> reselectAfterSeek(msg.annotation())));

        disposables.add(observable
                .ofType(OpenDoneEvent.class)
                .filter(evt -> evt.getUuid().equals(io.getUuid()))
                .subscribe(evt -> videoOpened()));
    }

    private void videoOpened() {
        openDone = true;
        handle(AnnotationSnapshots.snapshot(toolBox), Action.Add);
    }

    private void handle(Collection<Annotation> annotations, Action action) {
        if (!openDone) {
            return;
        }
//        var media = toolBox.getData().getMedia();
        List<LocalizationPair> pairs = LocalizedAnnotation.from(annotations)
                .stream()
                .flatMap(la -> la.toLocalization(toolBox).stream().map(loc -> new LocalizationPair(la, loc)))
                .toList();
        List<Localization> localizations = pairs.stream()
                .map(LocalizationPair::localization)
                .toList();

        log.atDebug().log(() -> "Outgoing to Sharktopoda: %s on %d localizations".formatted(action, localizations.size()));

        // An update can add or remove bounding boxes from an annotation, so it is handled even when
        // the annotations have no localizations left.
        if (action == Action.Update) {
            update(annotations, pairs);
        }
        else if (!localizations.isEmpty()) {
            var uuids = localizations.stream()
                    .map(Localization::getUuid)
                    .toList();
            switch (action) {
                case Add -> {
                    sentLocalizations.added(toUuidMap(pairs));
                    io.send(new AddLocalizationsCmd(io.getUuid(), localizations));
                }
                case Remove -> {
                    sentLocalizations.removed(uuids);
                    io.send(new RemoveLocalizationsCmd(io.getUuid(), uuids));
                }
                case Select -> {
                    if (sharktopodaState.shouldSendSelection(uuids)) {
                        sharktopodaState.setSelectedLocalizations(uuids);
                        io.send(new SelectLocalizationsCmd(io.getUuid(), uuids));
                    }
                }
            }
        }

        if (action.equals(Action.Select) && localizations.isEmpty() && !annotations.isEmpty()) {
            List<UUID> nothing = Collections.emptyList();
            sharktopodaState.setSelectedLocalizations(nothing);
            io.send(new SelectLocalizationsCmd(io.getUuid(), nothing));
        }
    }

    /**
     * Sharktopoda clears its localization selection whenever it seeks. After a seek to an annotation
     * that is selected in VARS, select its localizations again so they're highlighted at the new frame.
     * This is sent after the seek command, and Sharktopoda processes commands in order, so it lands
     * after the seek's clear. Sharktopoda doesn't report selections the client makes, so this can't
     * echo back. (The empty selection Sharktopoda reports for the seek itself is ignored by
     * IncomingController.)
     */
    private void reselectAfterSeek(Annotation annotation) {
        if (!openDone || annotation == null) {
            return;
        }
        var media = toolBox.getData().getMedia();
        if (media == null || !Objects.equals(media.getVideoReferenceUuid(), annotation.getVideoReferenceUuid())) {
            // Seeked to an annotation from a concurrent video. Sharktopoda doesn't have its localizations
            return;
        }
        var observationUuid = annotation.getObservationUuid();
        boolean selectedInVars = observationUuid != null && AnnotationSnapshots.selected(toolBox)
                .stream()
                .anyMatch(a -> observationUuid.equals(a.getObservationUuid()));
        if (!selectedInVars) {
            // Highlighting it would make Sharktopoda's selection disagree with VARS'
            return;
        }
        var uuids = LocalizedAnnotation.from(List.of(annotation))
                .stream()
                .flatMap(la -> la.toLocalization(toolBox).stream())
                .map(Localization::getUuid)
                .toList();
        if (uuids.isEmpty()) {
            return;
        }
        log.atDebug().log(() -> "Outgoing to Sharktopoda: Re-select %d localizations after seek".formatted(uuids.size()));
        sharktopodaState.setSelectedLocalizations(uuids);
        io.send(new SelectLocalizationsCmd(io.getUuid(), uuids));
    }

    /**
     * Sharktopoda ignores updates to localizations it doesn't have, so new bounding boxes on a changed
     * annotation are sent as adds, and bounding boxes that were removed from it are sent as removes.
     */
    private void update(Collection<Annotation> annotations, List<LocalizationPair> pairs) {
        var observationUuids = annotations.stream()
                .map(Annotation::getObservationUuid)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        var changes = sentLocalizations.changed(observationUuids, toUuidMap(pairs));

        if (!changes.toRemove().isEmpty()) {
            io.send(new RemoveLocalizationsCmd(io.getUuid(), List.copyOf(changes.toRemove())));
        }
        var toAdd = select(pairs, changes.toAdd());
        if (!toAdd.isEmpty()) {
            io.send(new AddLocalizationsCmd(io.getUuid(), toAdd));
        }
        var toUpdate = select(pairs, changes.toUpdate());
        if (!toUpdate.isEmpty()) {
            io.send(new UpdateLocalizationsCmd(io.getUuid(), toUpdate));
        }
    }

    private static List<Localization> select(List<LocalizationPair> pairs, Set<UUID> localizationUuids) {
        return pairs.stream()
                .map(LocalizationPair::localization)
                .filter(loc -> localizationUuids.contains(loc.getUuid()))
                .toList();
    }

    /** @return Localization UUID -> observation UUID */
    private static Map<UUID, UUID> toUuidMap(List<LocalizationPair> pairs) {
        var map = new HashMap<UUID, UUID>();
        for (var p : pairs) {
            var localizationUuid = p.localization().getUuid();
            var observationUuid = p.localizedAnnotation().annotation().getObservationUuid();
            if (localizationUuid != null && observationUuid != null) {
                map.put(localizationUuid, observationUuid);
            }
        }
        return map;
    }

    public void close() {
        disposables.forEach(Disposable::dispose);
        disposables.clear();
    }
}
