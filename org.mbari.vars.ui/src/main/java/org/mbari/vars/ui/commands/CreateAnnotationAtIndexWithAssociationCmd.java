package org.mbari.vars.ui.commands;

import org.mbari.vars.core.EventBus;
import org.mbari.vars.core.util.Preconditions;
import org.mbari.vars.services.ConceptService;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.events.AnnotationsAddedEvent;
import org.mbari.vars.ui.events.AnnotationsRemovedEvent;
import org.mbari.vars.ui.events.AnnotationsSelectedEvent;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;
import org.mbari.vcr4j.VideoIndex;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2018-05-11T13:20:00
 */
public class CreateAnnotationAtIndexWithAssociationCmd implements Command  {

    private final String concept;
    private final VideoIndex videoIndex;
    private final Association associationTemplate;
    private final Object eventSource;
    private final UUID transientKey = UUID.randomUUID();
    private volatile Annotation annotation;

    /**
     *
     * @param videoIndex The index for the annotation
     * @param concept
     * @param associationTemplate An association based on this template will
     *                            be added along to the annotation. If provided it must
     *                            have a UUID set (required to track the association)
     */
    public CreateAnnotationAtIndexWithAssociationCmd(VideoIndex videoIndex,
                                                     String concept,
                                                     Association associationTemplate) {
        this(videoIndex, concept, associationTemplate, "");
    }

    public CreateAnnotationAtIndexWithAssociationCmd(VideoIndex videoIndex,
                                                     String concept,
                                                     Association associationTemplate,
                                                     Object eventSource) {
        // This is no longer used. We can't preset the UUID anymore as it's generated by the server
//        Preconditions.checkArgument(associationTemplate.getUuid() != null, "The associationTemplate must have a UUID");
        this.videoIndex = videoIndex;
        this.concept = concept;
        this.associationTemplate = associationTemplate;
        this.eventSource = eventSource;
    }

    @Override
    public void apply(UIToolBox toolBox) {
        ConceptService conceptService = toolBox.getServices().getConceptService();

        conceptService
                .findConcept(concept)
                .thenCompose(opt ->
                    opt.map(value -> CompletableFuture.supplyAsync(() -> value)).orElseGet(conceptService::findRoot))
                .thenAccept(concept -> {
                    String primaryName = concept.getName();
                    Annotation a = CommandUtil.buildAnnotation(toolBox.getData(),
                            primaryName, videoIndex);
                    a.setTransientKey(transientKey);
                    Association as = new Association(associationTemplate);
                    a.setAssociations(Collections.singletonList(as));
                    toolBox.getServices()
                            .getAnnotationService()
                            .createAnnotations(Collections.singletonList(a))
                            .thenAccept(a1 -> {
                                var match = a1.stream()
                                        .filter(anno -> {
                                            Optional<Association> opt = anno.getAssociations()
                                                    .stream()
                                                    .max(Comparator.comparing(Association::getLastUpdatedTime));
                                            return opt.map(a2 ->
                                                a2.getLinkName().equals(as.getLinkName())
                                                        && a2.getLinkValue().equals(as.getLinkValue())
                                                        && a2.getToConcept().equals(as.getToConcept())
                                            ).orElse(false);
                                        })
                                        .findFirst();

                                match.ifPresent(anno -> {
                                    annotation = anno;
                                    annotation.setTransientKey(transientKey);
                                    EventBus eventBus = toolBox.getEventBus();
                                    eventBus.send(new AnnotationsAddedEvent(eventSource, List.of(annotation)));
                                    eventBus.send(new AnnotationsSelectedEvent(annotation));
                                });

                            });
                });
    }

    @Override
    public void unapply(UIToolBox toolBox) {
        if (annotation != null) {
            toolBox.getServices()
                    .getAnnotationService()
                    .deleteAnnotation(annotation.getObservationUuid())
                    .thenAccept(a -> {
                        toolBox.getEventBus()
                                .send(new AnnotationsRemovedEvent(annotation));
                        annotation = null;
                    });
        }
    }

    @Override
    public String getDescription() {
        return "Create Annotation using " + concept + " with " + associationTemplate;
    }
}
