package org.mbari.vars.ui.mediaplayers.sharktopoda2;

import io.reactivex.rxjava3.disposables.Disposable;
import org.mbari.vars.core.EventBus;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Media;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.events.AnnotationsAddedEvent;
import org.mbari.vars.ui.events.AnnotationsChangedEvent;
import org.mbari.vars.ui.events.AnnotationsRemovedEvent;
import org.mbari.vars.ui.events.AnnotationsSelectedEvent;
import org.mbari.vars.ui.mediaplayers.sharktopoda.localization.LocalizationController;
import org.mbari.vcr4j.remote.control.RVideoIO;
import org.mbari.vcr4j.remote.control.commands.localization.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class OutgoingController {

    private static final Logger log = LoggerFactory.getLogger(OutgoingController.class);

    private final UIToolBox toolBox;
    private final RVideoIO io;
    private final List<Disposable> disposables = new ArrayList<>();
    private final SharktopodaState sharktopodaState;
    private enum Action {
        Add, Clear, Remove, Select, Update
    }

    public OutgoingController(UIToolBox toolBox,
                              RVideoIO io,
                              SharktopodaState sharktopodaState) {
        this.toolBox = toolBox;
        this.io = io;
        this.sharktopodaState = sharktopodaState;
        init(toolBox.getEventBus());
    }

    private void init(EventBus eventBus) {
        disposables.add(eventBus.toObserverable()
                .ofType(AnnotationsAddedEvent.class)
                .filter(evt -> evt.getEventSource() != LocalizationController.EVENT_SOURCE)
                .filter(evt -> !evt.get().isEmpty())
                .subscribe(evt -> handle(evt.get(), Action.Add)));

        disposables.add(eventBus.toObserverable()
                .ofType(AnnotationsRemovedEvent.class)
                .filter(evt -> evt.getEventSource() != LocalizationController.EVENT_SOURCE)
                .filter(evt -> !evt.get().isEmpty())
                .subscribe(evt -> handle(evt.get(), Action.Remove)));

        disposables.add(eventBus.toObserverable()
                .ofType(AnnotationsChangedEvent.class)
                .filter(evt -> evt.getEventSource() != LocalizationController.EVENT_SOURCE)
                .filter(evt -> !evt.get().isEmpty())
                .subscribe(evt -> handle(evt.get(), Action.Update)));

        disposables.add(eventBus.toObserverable()
                .ofType(AnnotationsSelectedEvent.class)
                .filter(evt -> evt.getEventSource() != LocalizationController.EVENT_SOURCE)
                .subscribe(evt -> handle(evt.get(), Action.Select)));
    }

    private void handle(Collection<Annotation> annotations, Action action) {

        List<LocalizationPair> localizationPairs = SharktopodaState.from(annotations, toolBox);

        List<Localization> localizations = localizationPairs.stream()
                        .map(LocalizationPair::localization)
                        .toList();

        log.atDebug().log(() -> "Outgoing to Sharktopoda: %s on %d localizations".formatted(action, localizations.size()));

        if (!localizations.isEmpty()) {
            var uuids = localizations.stream()
                    .map(Localization::getUuid)
                    .toList();
            switch (action) {
                case Add -> {
                    sharktopodaState.addLocalizationPairs(localizationPairs);
                    io.send(new AddLocalizationsCmd(io.getUuid(), localizations));
                }
                case Update -> {
                    // TODO For Changed events we need to remove any localizations that
                    // on the annotation that no longer exist

                    // TODO for changed events we may have to add a new localization that
                    // was created on the VARS side
                    sharktopodaState.addLocalizationPairs(localizationPairs);
                    io.send(new UpdateLocalizationsCmd(io.getUuid(), localizations));
                }
                case Remove -> {
                    sharktopodaState.removeLocalizationPairs(localizationPairs);
                    io.send(new RemoveLocalizationsCmd(io.getUuid(), uuids));
                }
                case Select -> {
                    if (sharktopodaState.isDifferentThanSelected(uuids)) {
                        sharktopodaState.setSelectedLocalizations(localizationPairs);
                        io.send(new SelectLocalizationsCmd(io.getUuid(), uuids));
                    }
                }
            }
        }

        if (action.equals(Action.Select) && localizations.isEmpty() && !annotations.isEmpty()) {
            sharktopodaState.setSelectedLocalizations(Collections.emptyList());
            io.send(new SelectLocalizationsCmd(io.getUuid(), Collections.emptyList()));
        }
    }

    public void close() {
        disposables.forEach(Disposable::dispose);
        disposables.clear();
    }
}
