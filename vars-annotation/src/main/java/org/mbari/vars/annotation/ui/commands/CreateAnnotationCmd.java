package org.mbari.vars.annotation.ui.commands;

import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.events.AnnotationsAddedEvent;
import org.mbari.vars.annotation.ui.events.AnnotationsRemovedEvent;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;

import java.util.UUID;


/**
 * @author Brian Schlining
 * @since 2017-05-10T10:02:00
 */
public class CreateAnnotationCmd implements Command {
    private final Annotation annotationTemplate;
    private Annotation annotation;
    private final Object transientKey = UUID.randomUUID();

    public CreateAnnotationCmd(Annotation annotationTemplate) {
        this.annotationTemplate = annotationTemplate;
    }

    @Override
    public void apply(UIToolBox toolBox) {
        // Timecode/elapsedtime should have already been captured  from video
        toolBox.getServices()
                .conceptService()
                .findDetails(annotation.getConcept())
                .thenAccept(opt -> {
                    if (opt.isPresent()) {
                        // Update to primary name
                        annotationTemplate.setConcept(opt.get().getName());
                        toolBox.getServices()
                                .annotationService()
                                .createAnnotation(annotationTemplate)
                                .thenAccept(a -> {
                                    annotation = a;
                                    annotation.setTransientKey(transientKey);
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
                    .annotationService()
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