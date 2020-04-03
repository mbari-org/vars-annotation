package org.mbari.vars.ui.commands;

import org.mbari.vars.core.EventBus;
import org.mbari.vars.services.ConceptService;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.events.AnnotationsAddedEvent;
import org.mbari.vars.ui.events.AnnotationsRemovedEvent;
import org.mbari.vars.ui.events.AnnotationsSelectedEvent;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;
import org.mbari.vcr4j.VideoIndex;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * @author Brian Schlining
 * @since 2018-05-11T13:20:00
 */
public class CreateAnnotationAtIndexCmd implements Command  {

    private final String concept;
    private final VideoIndex videoIndex;
    private final Association associationTemplate;
    private volatile Annotation annotation;

    /**
     *
     * @param videoIndex The index for the annotation
     * @param concept
     * @param associationTemplate An association based on this template will
     *                            be added along to the annotation. If null,
     *                            then no association will be added
     */
    public CreateAnnotationAtIndexCmd(VideoIndex videoIndex, String concept, Association associationTemplate) {
        this.videoIndex = videoIndex;
        this.concept = concept;
        this.associationTemplate = associationTemplate;
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
                    if (associationTemplate != null) {
                        Association as = new Association(associationTemplate);
                        a.setAssociations(Collections.singletonList(as));
                    }
                    toolBox.getServices()
                            .getAnnotationService()
                            .createAnnotations(Collections.singletonList(a))
                            .thenAccept(a1 -> {
                                annotation = a1.stream().findFirst().orElse(null);
                                if (annotation != null) {
                                    EventBus eventBus = toolBox.getEventBus();
                                    eventBus.send(new AnnotationsAddedEvent(annotation));
                                    eventBus.send(new AnnotationsSelectedEvent(annotation));
                                }
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
