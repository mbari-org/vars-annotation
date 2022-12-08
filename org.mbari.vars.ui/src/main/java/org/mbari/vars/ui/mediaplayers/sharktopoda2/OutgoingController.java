package org.mbari.vars.ui.mediaplayers.sharktopoda2;

import io.reactivex.rxjava3.disposables.Disposable;
import org.mbari.vars.core.EventBus;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.ui.events.AnnotationsAddedEvent;
import org.mbari.vars.ui.events.AnnotationsChangedEvent;
import org.mbari.vars.ui.events.AnnotationsRemovedEvent;
import org.mbari.vars.ui.events.AnnotationsSelectedEvent;
import org.mbari.vars.ui.mediaplayers.sharktopoda.localization.LocalizationController;
import org.mbari.vcr4j.remote.control.RVideoIO;
import org.mbari.vcr4j.remote.control.commands.localization.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class OutgoingController {

    private final RVideoIO io;
    private final List<Disposable> disposables = new ArrayList<>();
    private enum Action {
        Add, Remove, Select, Update
    }

    public OutgoingController(EventBus eventBus, RVideoIO io) {
        this.io = io;
        init(eventBus);
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
        List<Localization> localizations = LocalizedAnnotation.from(annotations)
                .stream()
                .flatMap(opt -> opt.localization().stream())
                .toList();

        if (!localizations.isEmpty()) {
            var uuids = localizations.stream()
                    .map(Localization::getUuid)
                    .toList();
            switch (action) {
                case Add -> io.send(new AddLocalizationsCmd(io.getUuid(), localizations));
                case Update -> io.send(new UpdateLocalizationsCmd(io.getUuid(), localizations));
                case Remove -> io.send(new RemoveLocalizationsCmd(io.getUuid(), uuids));
                case Select -> io.send(new SelectLocalizationsCmd(io.getUuid(), uuids));
            }
        }
    }


    public void close() {
        disposables.forEach(Disposable::dispose);
    }
}
