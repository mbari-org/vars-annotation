package org.mbari.vars.ui.commands;

import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;
import org.mbari.vars.services.AnnotationService;
import org.mbari.vars.ui.javafx.AnnotationServiceDecorator;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Add an associa
 * @author Brian Schlining
 * @since 2017-07-20T17:35:00
 */
public class CreateAssociationsCmd implements Command {

    private final Association associationTemplate;
    private final Collection<Annotation> originalAnnotations;
    private Collection<Association> addedAssociations = new CopyOnWriteArrayList<>();

    public CreateAssociationsCmd(Association associationTemplate, Collection<Annotation> originalAnnotations) {
        this.associationTemplate = associationTemplate;
        this.originalAnnotations = Collections.unmodifiableCollection(originalAnnotations);

    }

    public Association getAssociationTemplate() {
        return associationTemplate;
    }

    @Override
    public void apply(UIToolBox toolBox) {
        AnnotationService annotationService = toolBox.getServices().getAnnotationService();
        CompletableFuture[] futures = originalAnnotations.stream()
                .map(anno -> annotationService.createAssociation(anno.getObservationUuid(), associationTemplate)
                        .thenAccept(association -> addedAssociations.add(association)))
                .toArray(i -> new CompletableFuture[i]);
        CompletableFuture.allOf(futures)
                .thenAccept(v -> {
                    AnnotationServiceDecorator asd = new AnnotationServiceDecorator(toolBox);
                    Set<UUID> uuids = originalAnnotations.stream()
                            .map(Annotation::getObservationUuid)
                            .collect(Collectors.toSet());
                    asd.refreshAnnotationsView(uuids);
                });
    }

    @Override
    public void unapply(UIToolBox toolBox) {
        AnnotationService annotationService = toolBox.getServices().getAnnotationService();
        AnnotationServiceDecorator asd = new AnnotationServiceDecorator(toolBox);
        List<UUID> uuids = addedAssociations.stream()
                .map(Association::getUuid)
                .collect(Collectors.toList());
        annotationService.deleteAssociations(uuids)
                .thenAccept(v -> {
                    addedAssociations.clear();
                    Set<UUID> uuids0 = originalAnnotations.stream()
                            .map(Annotation::getObservationUuid)
                            .collect(Collectors.toSet());
                    asd.refreshAnnotationsView(uuids0);
                });

    }

    @Override
    public String getDescription() {
        return "Add Association: " + associationTemplate;
    }
}
