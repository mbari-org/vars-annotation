package org.mbari.vars.ui.javafx.localization;

import com.google.gson.Gson;
import io.reactivex.disposables.Disposable;
import javafx.collections.ListChangeListener;
import org.mbari.vars.core.EventBus;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;
import org.mbari.vars.ui.Data;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.sharktopoda.client.localization.IO;
import org.mbari.vcr4j.sharktopoda.client.localization.Localization;
import org.mbari.vcr4j.sharktopoda.client.localization.Message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

class IncomingController {
    private final EventBus eventBus;
    private final IO io;
    private final Gson gson;
    private final List<Disposable> disposables = new ArrayList<>();
    private final Data data;

    public IncomingController(EventBus eventBus, IO io, Gson gson, Data data) {
        this.eventBus = eventBus;
        this.io = io;
        this.gson = gson;
        this.data = data;
        io.getController()
                .getLocalizations()
                .addListener((ListChangeListener<Localization>) c -> {
                    while (c.next()) {
                        if (c.wasAdded()) {
                            List<? extends Localization> addedSubList = c.getAddedSubList();
                            localizationsToAnnotations(addedSubList);
                        }
                    }
                });
        disposables.add(io.getController()
                .getIncoming()
                .ofType(Message.class)
                .subscribe(this::handleIncomingMessage));
    }

    public void handleIncomingMessage(Message message) {
        switch (message.getAction()) {
            case Message.ACTION_ADD:

        }
    }

    public List<Annotation> localizationsToAnnotations(Collection<? extends Localization> xs) {
        List<Annotation> annotations = new ArrayList<>();
        for (Localization x : xs) {
            // Does the annotation already exist?
            VideoIndex videoIndex = new VideoIndex(x.getElapsedTime());

            Annotation a = new Annotation(x.getConcept(),
                    data.getUser().getUsername(),
                    videoIndex,
                    x.getVideoReferenceUuid());
            a.setDuration(x.getDuration());

            BoundingBox bb = new BoundingBox(x.getX(), x.getY(), x.getWidth(), x.getHeight());
            String json = gson.toJson(bb);


            Association ass = new Association("bounding box",
                    Association.VALUE_SELF,
                    json,
                    "application/json");


//            Optional<Annotation> opt = data.getAnnotations()
//                    .stream()
//                    .filter(a -> a.getObservationUuid().equals(x.getAnnotationUuid()))
//                    .findFirst();
//
//            if (opt.isPresent()) {
//                // existing annotation
//            }


            // Does it have this localization (by assocation uuid = localizationUuid)

            //
        }
        return null;
    }
}
