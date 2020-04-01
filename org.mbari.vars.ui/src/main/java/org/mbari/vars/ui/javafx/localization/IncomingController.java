package org.mbari.vars.ui.javafx.localization;

import com.google.gson.Gson;
import io.reactivex.disposables.Disposable;
import javafx.collections.ListChangeListener;
import org.mbari.vars.core.EventBus;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;
import org.mbari.vars.ui.Data;
import org.mbari.vars.ui.commands.CreateAssociationsCmd;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.sharktopoda.client.localization.IO;
import org.mbari.vcr4j.sharktopoda.client.localization.Localization;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class IncomingController implements Closeable {
    private final EventBus eventBus;
    private final IO io;
    private final Gson gson;
    private final List<Disposable> disposables = new ArrayList<>();
    private final Data data;
    private final ListChangeListener<Localization> changeListener = c -> {
        while (c.next()) {
            if (c.wasAdded()) {
                List<? extends Localization> addedSubList = c.getAddedSubList();
                if (!addedSubList.isEmpty()) {
                    addLocalizations(addedSubList);
                }
            }
        }
    };

    public IncomingController(EventBus eventBus, IO io, Gson gson, Data data) {
        this.eventBus = eventBus;
        this.io = io;
        this.gson = gson;
        this.data = data;
        io.getController()
                .getLocalizations()
                .addListener(changeListener);
    }


    public List<Annotation> localizationsToAnnotations(Collection<? extends Localization> xs) {
        List<Annotation> annotations = new ArrayList<>();
        for (Localization x : xs) {
            // Does the annotation already exist?
            VideoIndex videoIndex = new VideoIndex(x.getElapsedTime());

            Optional<Annotation> opt = annotations.stream()
                    .filter(a -> a.getObservationUuid().equals(x.getAnnotationUuid()))
                    .findFirst();

            Annotation annotation = opt.orElseGet(() -> {
                Annotation a = new Annotation(x.getConcept(),
                        data.getUser().getUsername(),
                        videoIndex,
                        x.getVideoReferenceUuid());
                a.setDuration(x.getDuration());
                a.setObservationUuid(x.getAnnotationUuid());
                annotations.add(a);
                return a;
            });

            BoundingBox bb = new BoundingBox(x.getX(), x.getY(), x.getWidth(), x.getHeight());
            String json = gson.toJson(bb);

            Association ass = new Association("bounding box",
                    Association.VALUE_SELF,
                    json,
                    "application/json");

            annotation.getAssociations()
                    .add(ass);

        }
        return annotations;
    }

    private void addLocalizations(Collection<? extends Localization> xs) {
        // For now we're assuming that any externally created bounding box is a new annotation
        List<Annotation> annotations = localizationsToAnnotations(xs);
        List<Annotation> existingAnnotations = new ArrayList<>(data.getAnnotations());
        annotations.removeAll(existingAnnotations);
        annotations.stream()
                .forEach(annotation -> {
                    Command cmd = new CreateAssociationsCmd()
                });

        List<Association> newAssociations = annotations.stream()
                .flatMap(a -> a.getAssociations().stream())
                .collect(Collectors.toList());




    }

    public void close() {
        disposables.forEach(Disposable::dispose);
        io.getController()
                .getLocalizations()
                .removeListener(changeListener);
    }
}
