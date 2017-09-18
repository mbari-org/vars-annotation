package org.mbari.m3.vars.annotation.commands;

import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.events.AnnotationsChangedEvent;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.User;

import java.time.Instant;
import java.util.List;

/**
 * @author Brian Schlining
 * @since 2017-09-17T16:24:00
 */
public abstract class UpdateAnnotationsCmd implements Command {

    protected List<Annotation> originalAnnotations;
    protected List<Annotation> changedAnnotations;

    public UpdateAnnotationsCmd(List<Annotation> originalAnnotations, List<Annotation> changedAnnotations) {
        this.originalAnnotations = originalAnnotations;
        this.changedAnnotations = changedAnnotations;
        final Instant now = Instant.now();
        changedAnnotations.forEach(a -> a.setObservationTimestamp(now));
    }

    @Override
    public void apply(UIToolBox toolBox) {
        final User user = toolBox.getData().getUser();
        if (user != null) {
            changedAnnotations.forEach(a -> a.setObserver(user.getUsername()));
        }
        doUpdate(toolBox, changedAnnotations);
    }

    @Override
    public void unapply(UIToolBox toolBox) {
        doUpdate(toolBox, originalAnnotations);
    }

    private void doUpdate(UIToolBox toolBox, List<Annotation> annotations) {
        toolBox.getServices()
                .getAnnotationService()
                .updateAnnotations(annotations)
                .thenAccept(as -> {
                    toolBox.getEventBus()
                            .send(new AnnotationsChangedEvent(as));
                });
    }


}
