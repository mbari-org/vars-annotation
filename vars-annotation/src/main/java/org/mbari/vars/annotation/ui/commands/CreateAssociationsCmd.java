package org.mbari.vars.ui.commands;

import org.mbari.vars.services.ConceptService;
import org.mbari.vars.services.model.ConceptDetails;
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
        ConceptService conceptService = toolBox.getServices().getConceptService();

        // Check that the toConcept is a primary name if it is actually in the knowledgebase. If not
        // then we will use the template as is.
        conceptService.findDetails(associationTemplate.getToConcept())
                .thenAccept(opt -> {

                    final var finalTemplate = getAssociation(opt);
                    var futures = originalAnnotations.stream()
                            .map(anno -> annotationService.createAssociation(anno.getObservationUuid(), finalTemplate)
                                    .thenAccept(association -> addedAssociations.add(association)))
                            .toArray(CompletableFuture[]::new);

                    CompletableFuture.allOf(futures)
                            .thenAccept(v -> {
                                AnnotationServiceDecorator asd = new AnnotationServiceDecorator(toolBox);
                                Set<UUID> uuids = originalAnnotations.stream()
                                        .map(Annotation::getObservationUuid)
                                        .collect(Collectors.toSet());
                                asd.refreshAnnotationsView(uuids);
                            });

                });

    }

    /**
     * If the toConcept is not present in the database then we will use the template as is. Otherwise
     * @param opt
     * @return
     */
    private Association getAssociation(Optional<ConceptDetails> opt) {
        var template = associationTemplate;
        if (opt.isPresent()) {
            var conceptDetails = opt.get();
            if (!conceptDetails.getName().equals(template.getToConcept())) {
                template = new Association(associationTemplate.getLinkName(),
                        conceptDetails.getName(),
                        associationTemplate.getLinkValue(),
                        associationTemplate.getMimeType());
            }
        }

        return template;
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
