package org.mbari.vars.ui.mediaplayers.sharktopoda2;

import io.reactivex.rxjava3.disposables.Disposable;
import org.mbari.vars.core.EventBus;
import org.mbari.vcr4j.remote.control.commands.localization.*;
import org.mbari.vcr4j.remote.player.RxControlRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IncomingController {

    private final RxControlRequestHandler requestHandler;
    private final EventBus eventBus;
    private final List<Disposable> disposables = new ArrayList<>();
    private static final Logger log = LoggerFactory.getLogger(IncomingController.class);

    public IncomingController(EventBus eventBus, RxControlRequestHandler requestHandler) {
        this.requestHandler = requestHandler;
        this.eventBus = eventBus;
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




    }

    private void handleAdd(List<Localization> added) {

    }

    private void handleRemove(List<UUID> removed) {

    }

    private void handleUpdate(List<Localization> updated) {

    }

    private void handleSelect(List<UUID> selected) {

    }
}
