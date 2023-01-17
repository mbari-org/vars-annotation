package org.mbari.vars.ui.mediaplayers.sharktopoda.localization;

import com.google.gson.Gson;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import javafx.collections.ListChangeListener;
import org.mbari.vars.core.EventBus;
import org.mbari.vars.services.model.Association;
import org.mbari.vars.services.model.BoundingBox;
import org.mbari.vars.services.model.Media;
import org.mbari.vars.ui.Data;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.commands.Command;
import org.mbari.vars.ui.commands.CreateAnnotationAtIndexWithAssociationCmd;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.sharktopoda.client.localization.IO;
import org.mbari.vcr4j.sharktopoda.client.localization.Localization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller that handles inbound traffic from remote app that's creating localizations.
 * @deprecated  Use IncomingController2 instead
 */
@Deprecated(since="2022-01-01")
class IncomingController implements Closeable {
    private final UIToolBox toolBox;
    private final EventBus eventBus;
    private final IO io;
    private final Gson gson;
    private final Data data;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Subject<Localization> internalBus;

    /**
     * IMPORTANT: We're only handling new additions of annotations, not deletes.
     * nor updates
     */
    private final ListChangeListener<Localization> changeListener = c -> {
        // TODO add support for delete/update
        while (c.next()) {
            if (c.wasAdded()) {
                List<? extends Localization> addedSubList = c.getAddedSubList();
                if (!addedSubList.isEmpty()) {
                    addedSubList.forEach(x -> getInternalBus().onNext(x));
                }
            }
        }
    };

    /**
     * Constructor
     * @param io The IO Object used to connect via ZeroMQ to the remote app
     * @param gson For parsing the Localization
     * @param toolBox UIToolBox full of needed goodies
     */
    public IncomingController(IO io, Gson gson, UIToolBox toolBox) {
        this.eventBus = toolBox.getEventBus();
        this.io = io;
        this.gson = gson;
        this.data = toolBox.getData();
        this.toolBox = toolBox;
        Subject<Localization> bus = PublishSubject.create();
        internalBus = bus.toSerialized();
        internalBus
                .distinctUntilChanged(Localization::getLocalizationUuid)
                .subscribe(x -> handleAddedLocalizations(List.of(x)),
                e -> log.error("An exception occurred in the incoming localization observable", e));

        io.getController()
                .getLocalizations()
                .addListener(changeListener);

    }

    private Subject<Localization> getInternalBus() {
        return internalBus;
    }

    private void handleAddedLocalizations(Collection<? extends Localization> xs) {
        Media media = data.getMedia();
        if (media == null) {
            log.info("An attempt was made to add a localization, but no media is currently open");
            return;
        }
        // Does this localization already exist?
        Set<UUID> associationUuids = data.getAnnotations()
                .stream()
                .flatMap(a -> a.getAssociations().stream())
                .filter(a -> a.getLinkName().equalsIgnoreCase(BoundingBox.LINK_NAME))
                .map(Association::getUuid)
                .collect(Collectors.toSet());
        for (Localization x : xs) {
            if (associationUuids.contains(x.getLocalizationUuid())) {
                log.debug("An association with UUID of " + x.getLocalizationUuid() + " already exists. Skipping");
            }
            else {
                io.getController().removeLocalization(x);
                VideoIndex videoIndex = new VideoIndex(x.getElapsedTime());
                String concept = x.getConcept();
                Association template = toAssociation(x);
                Command cmd = new CreateAnnotationAtIndexWithAssociationCmd(videoIndex, concept, template);
                eventBus.send(cmd);
            }

        }
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

    /**
     * releases resources and cleans up
     */
    public void close() {
        io.getController()
                .getLocalizations()
                .removeListener(changeListener);
    }
}
