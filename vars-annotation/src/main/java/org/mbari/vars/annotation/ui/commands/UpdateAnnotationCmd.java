package org.mbari.vars.annotation.ui.commands;

import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.events.AnnotationsChangedEvent;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author Brian Schlining
 * @since 2017-05-10T10:05:00
 */
public class UpdateAnnotationCmd implements Command {

    private final Annotation oldAnnotation;
    private final Annotation newAnnotation;

    public UpdateAnnotationCmd(Annotation oldAnnotation, Annotation newAnnotation) {
        this.oldAnnotation = oldAnnotation;
        this.newAnnotation = newAnnotation;
    }

    @Override
    public void apply(UIToolBox toolBox) {
        newAnnotation.setObservationTimestamp(Instant.now());
        Optional.ofNullable(toolBox.getData().getUser())
                .ifPresent(user -> newAnnotation.setObserver(user.getUsername()));
        doAction(toolBox, newAnnotation);
    }

    @Override
    public void unapply(UIToolBox toolBox) {
        doAction(toolBox, oldAnnotation);
    }

    private void doAction(UIToolBox toolBox, Annotation annotation) {
        toolBox.getServices()
                .conceptService()
                .findDetails(annotation.getConcept())
                .thenAccept(opt -> {
                    if (opt.isPresent()) {
                        // Update to primary concept name
                        newAnnotation.setConcept(opt.get().getName());
                         toolBox.getServices()
                                .annotationService()
                                .updateAnnotation(annotation)
                                .thenAccept(a -> {
                                    a.setTransientKey(oldAnnotation.getTransientKey());
                                    toolBox.getEventBus()
                                            .send(new AnnotationsChangedEvent(null, List.of(a)));
                                });
                    }
                });

    }

    @Override
    public String getDescription() {
        return "Update annotation";
    }

    @Override
    public String toString() {
        return "UpdateAnnotationCmd{" +
                "oldAnnotation=" + oldAnnotation +
                ", newAnnotation=" + newAnnotation +
                '}';
    }
}