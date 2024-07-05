package org.mbari.vars.ui.mediaplayers.sharktopoda2;

import io.reactivex.rxjava3.disposables.Disposable;
import org.mbari.vars.core.EventBus;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.events.*;
import org.mbari.vars.ui.mediaplayers.sharktopoda.Constants;
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

        // #174: Force reload localizations in the video player
        disposables.add(observable
                .ofType(ForceReloadLocalizationsEvent.class)
                .subscribe(evt -> forceReload()));
    }

    /**
     * Clear and re-send the localizations to the video player
     */
    private void forceReload() {
        io.send(new ClearLocalizationsCmd(new ClearLocalizationsCmd.Request(io.getUuid())));
        handle(toolBox.getData().getAnnotations(), Action.Add);
    }

    private void handle(Collection<Annotation> annotations, Action action) {
//        var media = toolBox.getData().getMedia();
        List<Localization> localizations = LocalizedAnnotation.from(annotations)
                .stream()
                .flatMap(opt -> opt.toLocalization(toolBox).stream())
                .toList();

        log.atDebug().log(() -> "Outgoing to Sharktopoda: %s on %d localizations".formatted(action, localizations.size()));

        if (!localizations.isEmpty()) {
            var uuids = localizations.stream()
                    .map(Localization::getUuid)
                    .toList();
            switch (action) {
                case Add -> io.send(new AddLocalizationsCmd(io.getUuid(), localizations));
                case Update -> io.send(new UpdateLocalizationsCmd(io.getUuid(), localizations));
                case Remove -> io.send(new RemoveLocalizationsCmd(io.getUuid(), uuids));
                case Select -> {
                    if (sharktopodaState.isDifferentThanSelected(uuids)) {
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

    public void close() {
        disposables.forEach(Disposable::dispose);
        disposables.clear();
    }
}
