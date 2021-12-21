package org.mbari.vars.ui.mediaplayers.sharktopoda.localization;

import com.google.gson.Gson;
import javafx.collections.ListChangeListener;
import org.mbari.vars.core.EventBus;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;
import org.mbari.vars.services.model.Media;
import org.mbari.vars.ui.Data;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.commands.Command;
import org.mbari.vars.ui.commands.CreateAnnotationAtIndexWithAssociationCmd;
import org.mbari.vars.ui.commands.DeleteAssociationsCmd;
import org.mbari.vars.ui.commands.UpdateAssociationCmd;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.sharktopoda.client.localization.IO;
import org.mbari.vcr4j.sharktopoda.client.localization.Localization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class IncomingController2 implements Closeable {

    private final EventBus eventBus;
    private final IO io;
    private final Gson gson;
    private final Data data;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private record AnnoWithBox(Annotation annotation, Association association) {}
    private final Comparator<AnnoWithBox> abComparator = Comparator.comparing(a -> a.association().getUuid());

    private final ListChangeListener<Localization> changeListener = (c) -> {
        while (c.next()) {
            if (c.wasAdded()) {
                List<? extends Localization> addedSubList = c.getAddedSubList();
                handleAdd(addedSubList);
            }
            else if (c.wasRemoved()) {
                List<? extends Localization> removedSubList = c.getRemoved();
                handleRemove(removedSubList);
            }
        }
    };

    public IncomingController2(IO io, Gson gson, UIToolBox toolBox) {
        this.io = io;
        this.gson = gson;
        this.data = toolBox.getData();
        this.eventBus = toolBox.getEventBus();
        init();
    }

    private void init() {
        io.getController()
                .getLocalizations()
                .addListener(changeListener);
    }

    private void handleAdd(List<? extends Localization> added) {
        Media media = data.getMedia();
        if (media == null) {
            log.info("An attempt was made to add a localization, but no media is currently open");
            return;
        }

        // Does this localization already exist?
        List<AnnoWithBox> annosWithBox = data.getAnnotations()
                .stream()
                .flatMap(anno ->
                        anno.getAssociations()
                                .stream()
                                .filter(ass -> ass.getLinkName().equalsIgnoreCase(BoundingBox.LINK_NAME))
                                .map(ass -> new AnnoWithBox(anno, ass)))
                .sorted(abComparator)
                .collect(Collectors.toList());


        for (Localization x : added) {
            var xAss = toAssociation(x);
            int match = Collections.binarySearch(annosWithBox, new AnnoWithBox(null, xAss), abComparator);
            if (match >= 0) {
                log.debug("An association with UUID of " + x.getLocalizationUuid() + " already exists. Updating it");
                var existingAb = annosWithBox.get(match);
                Command cmd = new UpdateAssociationCmd(existingAb.annotation().getObservationUuid(),
                        existingAb.association(), xAss);
                eventBus.send(cmd);
            }
            else {
                // -- create a new annotation/association
//                io.getController().removeLocalization(x);
                VideoIndex videoIndex = new VideoIndex(x.getElapsedTime());
                String concept = x.getConcept();
                Association template = toAssociation(x);
                Command cmd = new CreateAnnotationAtIndexWithAssociationCmd(videoIndex, concept, template);
                eventBus.send(cmd);
            }

        }
    }

    private void handleRemove(List<? extends Localization> removed) {
        Media media = data.getMedia();
        if (media == null) {
            log.info("An attempt was made to remove a localization, but no media is currently open");
            return;
        }
        // Does this localization already exist?
        List<AnnoWithBox> annosWithBox = data.getAnnotations()
                .stream()
                .flatMap(anno ->
                        anno.getAssociations()
                                .stream()
                                .filter(ass -> ass.getLinkName().equalsIgnoreCase(BoundingBox.LINK_NAME))
                                .map(ass -> new AnnoWithBox(anno, ass)))
                .sorted(abComparator)
                .collect(Collectors.toList());
        for (Localization x : removed) {
            var xAss = toAssociation(x);
            int match = Collections.binarySearch(annosWithBox, new AnnoWithBox(null, xAss), abComparator);
            if (match >= 0) {
                var ab = annosWithBox.get(match);
                Command cmd = new DeleteAssociationsCmd(Map.of(ab.association(), ab.annotation().getObservationUuid()));
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

    @Override
    public void close() {
        io.getController()
                .getLocalizations()
                .removeListener(changeListener);
    }
}
