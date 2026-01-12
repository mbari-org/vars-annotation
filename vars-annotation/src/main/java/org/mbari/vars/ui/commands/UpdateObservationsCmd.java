package org.mbari.vars.ui.commands;

import org.mbari.vars.services.ConceptService;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.ObservationsUpdate;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.javafx.AnnotationServiceDecorator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class UpdateObservationsCmd implements Command {

    protected List<Annotation> originalAnnotations;
    protected ObservationsUpdate observationsUpdate;

    public UpdateObservationsCmd(List<Annotation> originalAnnotations, ObservationsUpdate observationsUpdate) {
        this.originalAnnotations = originalAnnotations;
        this.observationsUpdate = observationsUpdate;
        // The orignal annotations and the observation uuid s in the update much match
    }

    public static Set<UUID> observationUuids(List<Annotation> annotations) {
        return annotations.stream()
                .map(Annotation::getObservationUuid)
                .collect(Collectors.toSet());
    }

    @Override
    public void apply(UIToolBox toolBox) {

        Runnable runnable = () -> {
            if(observationsUpdate.concept() != null) {
                ConceptService conceptService = toolBox.getServices().getConceptService();
                var conceptOpt = conceptService.findConcept(observationsUpdate.concept()).join();
                if (conceptOpt.isEmpty()) {
                    throw new RuntimeException("Concept " + observationsUpdate.concept() + " does not exist");
                }
                else {
                    observationsUpdate = observationsUpdate.withConcept(conceptOpt.get().getName());
                }
            }
            var annotationService = toolBox.getServices().getAnnotationService();
            annotationService.updateObservations(observationsUpdate).join();
            AnnotationServiceDecorator asd = new AnnotationServiceDecorator(toolBox);
            Set<UUID> uuids = new HashSet<>(observationsUpdate.observationUuids());
            asd.refreshAnnotationsView(uuids);
        };
        Thread.ofVirtual().start(runnable);

    }

    @Override
    public void unapply(UIToolBox toolBox) {
        toolBox.getServices()
                .getAnnotationService()
                .updateAnnotations(originalAnnotations)
                .thenAccept(as -> {
                    AnnotationServiceDecorator asd = new AnnotationServiceDecorator(toolBox);
                    Set<UUID> uuids = originalAnnotations.stream()
                            .map(Annotation::getObservationUuid)
                            .collect(Collectors.toSet());
                    asd.refreshAnnotationsView(uuids);
                });
    }

}
