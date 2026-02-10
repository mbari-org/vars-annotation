package org.mbari.vars.annotation.ui.commands;


import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.events.AnnotationsAddedEvent;
import org.mbari.vars.annotation.ui.events.AnnotationsRemovedEvent;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annosaurus.sdk.r1.AnnotationService;
import org.mbari.vars.annotation.util.Preconditions;

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
        AnnotationService service = toolBox.getServices().annotationService();
        Collection<UUID> uuids = annotations.stream()
                .map(Annotation::getObservationUuid)
                .collect(Collectors.toList());
        service.deleteAnnotations(uuids)
                .thenAccept(v -> toolBox.getEventBus()
                        .send(new AnnotationsRemovedEvent(null, annotations)));

    }

    @Override
    public void unapply(UIToolBox toolBox) {
        AnnotationService service = toolBox.getServices().annotationService();
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
