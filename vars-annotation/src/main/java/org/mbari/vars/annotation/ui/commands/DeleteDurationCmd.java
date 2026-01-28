package org.mbari.vars.ui.commands;

import org.mbari.vars.services.AnnotationService;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.javafx.AnnotationServiceDecorator;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class DeleteDurationCmd implements Command {

    private final List<Annotation> originalAnnotations;

    public DeleteDurationCmd(List<Annotation> annotations) {
        originalAnnotations = annotations.stream()
                .filter(a -> a.getDuration() != null)
                .collect(Collectors.toList());
    }

    @Override
    public String getDescription() {
        return "Delete duration from " + originalAnnotations.size() + " annotations";
    }

    @Override
    public void apply(UIToolBox toolBox) {
        AnnotationService annotationService = toolBox.getServices().getAnnotationService();
        CompletableFuture[] futures = originalAnnotations.stream()
                .map(Annotation::getObservationUuid)
                .map(annotationService::deleteDuration)
                .toArray(i -> new CompletableFuture[i]);
        CompletableFuture.allOf(futures)
                .thenAccept(v -> refresh(toolBox, originalAnnotations));
    }

    @Override
    public void unapply(UIToolBox toolBox) {
        AnnotationService annotationService = toolBox.getServices().getAnnotationService();
        annotationService.updateAnnotations(originalAnnotations)
                .thenAccept(as -> refresh(toolBox, as));
    }

    private void refresh(UIToolBox toolBox, Collection<Annotation> as) {
        AnnotationServiceDecorator asd = new AnnotationServiceDecorator(toolBox);
        Set<UUID> uuids = as.stream()
                .map(Annotation::getObservationUuid)
                .collect(Collectors.toSet());
        asd.refreshAnnotationsView(uuids);
    }
}
