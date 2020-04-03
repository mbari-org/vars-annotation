package org.mbari.vars.ui.commands;

import org.mbari.vars.services.AnnotationService;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.events.AnnotationsRemovedEvent;
import org.mbari.vars.ui.javafx.AnnotationServiceDecorator;
import org.mbari.vcr4j.util.Preconditions;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This is a specialized create an annotation along with any dependents, like
 * imagedMoments or associations. This will also handle cases where the annotation,
 * or associations have predefined UUIDs
 *
 * If that is not your use case, use `CreateAssociationsCmd` instead.
 */
public class BulkCreateAnnotations implements Command {

    private final Collection<Annotation> annotations;
    private Collection<Annotation> addedAnnotations;

    public BulkCreateAnnotations(Collection<Annotation> annotations) {
        Preconditions.checkArgument(annotations != null, "You provided a null collection ... bad!!");
        Preconditions.checkArgument(!annotations.isEmpty(), "You are attempting to create annotations from an empty collection. You get nothing!!");
        this.annotations = annotations;
    }

    public Collection<Annotation> getAnnotations() {
        return annotations;
    }

    @Override
    public void apply(UIToolBox toolBox) {
        AnnotationService annotationService = toolBox.getServices().getAnnotationService();
        annotationService.createAnnotations(annotations)
                .thenAccept(annos -> {
                    addedAnnotations = annos;
                    AnnotationServiceDecorator asd = new AnnotationServiceDecorator(toolBox);
                    Set<UUID> observationUuids = addedAnnotations.stream()
                            .map(Annotation::getObservationUuid)
                            .collect(Collectors.toSet());
                    asd.refreshAnnotationsView(observationUuids);
                });
    }

    @Override
    public void unapply(UIToolBox toolBox) {
        if (addedAnnotations != null) {
            AnnotationService annotationService = toolBox.getServices().getAnnotationService();
            Set<UUID> observationUuids = addedAnnotations.stream()
                    .map(Annotation::getObservationUuid)
                    .collect(Collectors.toSet());
            annotationService.deleteAnnotations(observationUuids)
                    .thenAccept(annos -> {
                        addedAnnotations = null;
                        toolBox.getEventBus()
                                .send(new AnnotationsRemovedEvent(addedAnnotations));
                    });
        }
    }

    @Override
    public String getDescription() {
        return "Create " + annotations.size() + " annotations in bulk";
    }
}
