package org.mbari.vars.ui.mediaplayers.sharktopoda.localization;

import com.google.gson.Gson;
import io.reactivex.disposables.Disposable;
import org.mbari.vars.core.EventBus;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;
import org.mbari.vars.ui.events.AnnotationsAddedEvent;
import org.mbari.vars.ui.events.AnnotationsChangedEvent;
import org.mbari.vars.ui.events.AnnotationsRemovedEvent;
import org.mbari.vars.ui.events.AnnotationsSelectedEvent;
import org.mbari.vcr4j.sharktopoda.client.localization.IO;
import org.mbari.vcr4j.sharktopoda.client.localization.Localization;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Managed traffic from vars-annotation to external localization apps
 */
public class  OutgoingController implements Closeable {
    private final IO io;
    private final Gson gson;
    private final List<Disposable> disposables = new ArrayList<>();
    private enum Action {
        Add, Remove, Update
    }

    public OutgoingController(EventBus eventBus, IO io, Gson gson) {
        this.io = io;
        this.gson = gson;
        disposables.add(eventBus.toObserverable()
                .ofType(AnnotationsAddedEvent.class)
                .filter(evt -> evt.getEventSource() != LocalizationController.EVENT_SOURCE)
                .filter(evt -> !evt.get().isEmpty())
                .subscribe(this::handleAddedLocally));
        disposables.add(eventBus.toObserverable()
                .ofType(AnnotationsRemovedEvent.class)
                .filter(evt -> evt.getEventSource() != LocalizationController.EVENT_SOURCE)
                .filter(evt -> !evt.get().isEmpty())
                .subscribe(this::handleRemovedLocally));
        disposables.add(eventBus.toObserverable()
                .ofType(AnnotationsChangedEvent.class)
                .filter(evt -> evt.getEventSource() != LocalizationController.EVENT_SOURCE)
                .filter(evt -> !evt.get().isEmpty())
                .subscribe(this::handleChangedLocally));
        disposables.add(eventBus.toObserverable()
                .ofType(AnnotationsSelectedEvent.class)
                .filter(evt -> evt.getEventSource() != LocalizationController.EVENT_SOURCE)
                .subscribe(this::handleSelectedLocally));

    }

    private void handle(Collection<Annotation> annotations, Action action) {
        List<Localization> localizations = annotationsToLocalizations(annotations);
        if (!localizations.isEmpty()) {
            switch (action) {
                case Add:
                    io.getController().addLocalizations(localizations);
                    break;
                case Update:
                    io.getController().addLocalizations(localizations);
                    break;
                case Remove:
                    io.getController().removeLocalizations(localizations);
                    break;
            }
        }
    }

    public void handleAddedLocally(AnnotationsAddedEvent evt) {
        handle(evt.get(), Action.Add);
    }

    public void handleRemovedLocally(AnnotationsRemovedEvent evt) {
        handle(evt.get(), Action.Remove);
    }

    public void handleChangedLocally(AnnotationsChangedEvent evt) {
        handle(evt.get(), Action.Update);
    }

    public void handleSelectedLocally(AnnotationsSelectedEvent evt) {
        List<Localization> xs = annotationsToLocalizations(evt.get());
//        xs.forEach(x -> System.out.println("SENDING LOCALIZATION: " + gson.toJson(x)));
        io.getSelectionController()
                .select(xs, true);
    }

    public List<Localization> annotationsToLocalizations(Collection<Annotation> annotations) {
        List<Localization> xs = new ArrayList<>();
        for (Annotation a: annotations) {
            for (Association ass: a.getAssociations()) {
                if (ass.getLinkName().equalsIgnoreCase("bounding box") &&
                        ass.getMimeType().equalsIgnoreCase("application/json")) {
                    BoundingBox box = gson.fromJson(ass.getLinkValue(), BoundingBox.class);
                    Localization x = new Localization(a.getConcept(),
                            a.getElapsedTime(),
                            ass.getUuid(),
                            a.getVideoReferenceUuid(),
                            box.getX(),
                            box.getY(),
                            box.getWidth(),
                            box.getHeight(),
                            a.getDuration(),
                            a.getObservationUuid());
                    xs.add(x);
                }
            }
        }
        return xs;
    }

    public void close() {
        disposables.forEach(Disposable::dispose);
    }
}
