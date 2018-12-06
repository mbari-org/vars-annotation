package org.mbari.m3.vars.annotation.commands;

import com.google.common.collect.ImmutableList;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.m3.vars.annotation.services.AnnotationService;
import org.mbari.m3.vars.annotation.ui.AnnotationServiceDecorator;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-07-20T17:35:00
 */
public class CreateAssociationsCmd implements Command {

    private final Association associationTemplate;
    private final Collection<Annotation> originalAnnotations;
    private Collection<Association> addedAssociations = new CopyOnWriteArrayList<>();

    public CreateAssociationsCmd(Association associationTemplate, Collection<Annotation> originalAnnotations) {
        this.associationTemplate = associationTemplate;
        this.originalAnnotations = ImmutableList.copyOf(originalAnnotations);

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
