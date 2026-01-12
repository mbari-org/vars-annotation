package org.mbari.vars.ui.commands;

import org.mbari.vars.core.util.Preconditions;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.events.AnnotationsAddedEvent;
import org.mbari.vars.ui.events.AnnotationsRemovedEvent;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.AnnotationService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-05-11T13:06:00
 */
public class DeleteAnnotationsCmd implements Command {

    private volatile List<Annotation> annotations;

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
        Collection<UUID> uuids = annotations.stream()
                .map(Annotation::getObservationUuid)
                .collect(Collectors.toList());
        service.deleteAnnotations(uuids)
                .thenAccept(v -> toolBox.getEventBus()
                        .send(new AnnotationsRemovedEvent(null, annotations)));

    }

    @Override
    public void unapply(UIToolBox toolBox) {
        AnnotationService service = toolBox.getServices().getAnnotationService();
        service.createAnnotations(annotations)
            .thenAccept(as -> {
                annotations = Collections.unmodifiableList(new ArrayList<>(as));
                toolBox.getEventBus()
                        .send(new AnnotationsAddedEvent(null, annotations));
            });
    }

    @Override
    public String getDescription() {
        return "Delete " + annotations.size() + " annotations";
    }
}
