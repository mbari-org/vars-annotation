package org.mbari.vars.ui.mediaplayers.sharktopoda2;

import io.reactivex.rxjava3.disposables.Disposable;
import org.mbari.vars.core.EventBus;
import org.mbari.vars.services.model.BoundingBox;
import org.mbari.vars.services.model.Media;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.mediaplayers.sharktopoda.localization.IncomingController2;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.remote.control.commands.localization.*;
import org.mbari.vcr4j.remote.player.RxControlRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class IncomingController {

    private final RxControlRequestHandler requestHandler;
    private final UIToolBox toolBox;
    private final List<Disposable> disposables = new ArrayList<>();
    private static final Logger log = LoggerFactory.getLogger(IncomingController.class);
    private final Comparator<LocalizedAnnotation> abComparator = Comparator.comparing(a -> a.association().getUuid());

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

    }

    private void handleAdd(List<Localization> added) {
        Media media = toolBox.getData().getMedia();
        if (media == null) {
            log.info("An attempt was made to add a localization, but no media is currently open");
            return;
        }
        added.stream()
                .map(loc -> {
                    var elapsedTime = Duration.ofMillis(loc.getElapsedTimeMillis());
                    var videoIndex = new VideoIndex(elapsedTime);
                    var template = LocalizedAnnotation.from(loc);
                })

//        var existingLocalizations = toolBox.getData()
//                .getAnnotations()
//                .stream()
//                .flatMap(anno -> LocalizedAnnotation.from(anno).stream())
//                .sorted(abComparator)
//                .toList();
    }

    private void handleRemove(List<UUID> removed) {

    }

    private void handleUpdate(List<Localization> updated) {

    }

    private void handleSelect(List<UUID> selected) {

    }
}
