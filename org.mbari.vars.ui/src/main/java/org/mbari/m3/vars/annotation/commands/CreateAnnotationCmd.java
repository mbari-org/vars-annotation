package org.mbari.m3.vars.annotation.commands;

import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.events.AnnotationsAddedEvent;
import org.mbari.m3.vars.annotation.events.AnnotationsRemovedEvent;
import org.mbari.vars.services.model.Annotation;


/**
 * @author Brian Schlining
 * @since 2017-05-10T10:02:00
 */
public class CreateAnnotationCmd implements Command {
    private final Annotation annotationTemplate;
    private Annotation annotation;

    public CreateAnnotationCmd(Annotation annotationTemplate) {
        this.annotationTemplate = annotationTemplate;
    }

    @Override
    public void apply(UIToolBox toolBox) {
        // Timecode/elapsedtime should have already been captured  from video
        toolBox.getServices()
                .getConceptService()
                .findDetails(annotation.getConcept())
                .thenAccept(opt -> {
                    if (opt.isPresent()) {
                        // Update to primary name
                        annotationTemplate.setConcept(opt.get().getName());
                        toolBox.getServices()
                                .getAnnotationService()
                                .createAnnotation(annotationTemplate)
                                .thenAccept(a -> {
                                    annotation = a;
                                    toolBox.getEventBus()
                                            .send(new AnnotationsAddedEvent(a));
                                });
                    }
                });

    }

    @Override
    public void unapply(UIToolBox toolBox) {
        if (annotation != null) {
            toolBox.getServices()
                    .getAnnotationService()
                    .deleteAnnotation(annotation.getObservationUuid())
                    .thenAccept(b -> {
                       annotation = null;
                       toolBox.getEventBus().send(new AnnotationsRemovedEvent(annotation));
                    });
        }
    }


    @Override
    public String getDescription() {
        return "Create an annotation";
    }
}