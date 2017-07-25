package org.mbari.m3.vars.annotation.commands;

import com.google.common.base.Preconditions;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.events.AnnotationsAddedEvent;
import org.mbari.m3.vars.annotation.events.AnnotationsRemovedEvent;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.services.AnnotationService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-05-11T13:06:00
 */
public class DeleteAnnotationsCmd implements Command {

    private List<Annotation> annotations;

    public DeleteAnnotationsCmd(List<Annotation> annotations) {
        Preconditions.checkArgument(annotations != null,
                "Can not delete a null annotation list");
        Preconditions.checkArgument(!annotations.isEmpty(),
                "Can not delete an empty annotation list");
        this.annotations = Collections.unmodifiableList(new ArrayList<>(annotations));
    }

    @Override
    public void apply(UIToolBox toolBox) {
        AnnotationService service = toolBox.getServices().getAnnotationService();
        annotations.forEach(a -> service.deleteAnnotation(a.getObservationUuid()));
        toolBox.getEventBus()
                .send(new AnnotationsRemovedEvent(null, annotations));
    }

    @Override
    public void unapply(UIToolBox toolBox) {
        AnnotationService service = toolBox.getServices().getAnnotationService();
        final List<Annotation> annos = new CopyOnWriteArrayList<>();

        CompletableFuture[] futures = annotations.stream()
                .map(service::createAnnotation)
                .map(f -> f.thenApply(a -> {
                    annos.add(a);
                    return a;
                }))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures)
                .thenAccept(v -> {
                    annotations = annos;
                    new AnnotationsAddedEvent(null, annos);
                });

    }

    @Override
    public String getDescription() {
        return "Delete " + annotations.size() + " annotations";
    }
}
