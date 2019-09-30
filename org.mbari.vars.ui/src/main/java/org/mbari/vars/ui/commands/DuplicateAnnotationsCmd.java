package org.mbari.vars.ui.commands;

import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.events.AnnotationsAddedEvent;
import org.mbari.vars.ui.events.AnnotationsRemovedEvent;
import org.mbari.vars.ui.events.AnnotationsSelectedEvent;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;
import org.mbari.vars.services.model.Media;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-07-20T22:23:00
 */
public class DuplicateAnnotationsCmd implements Command {

    private final Collection<Annotation> duplicateAnnotations;
    private final boolean selectAnnotations;

    public DuplicateAnnotationsCmd(String user, String activity, Collection<Annotation> sourceAnnotations,
                                   boolean selectAnnotations) {
        this.duplicateAnnotations = sourceAnnotations.stream()
                .map(a -> makeDuplicate(a, user, activity))
                .collect(Collectors.toList());
        this.selectAnnotations = selectAnnotations;
    }

    public Annotation makeDuplicate(Annotation annotation, String user, String activity) {
        Annotation duplicate = new Annotation(annotation);
        duplicate.setObservationUuid(null);
        duplicate.setObservationTimestamp(Instant.now());
        duplicate.setObserver(user);
        duplicate.setActivity(activity);

        // M3-41 - video lab doesn't want associations copied
        duplicate.setAssociations(Collections.emptyList());

        // if we don't null the associations uuid, it will fail to insert due to
        // a duplicate primary key clash.
//        duplicate.getAssociations().forEach(Association::resetUuid);
        return duplicate;
    }

    @Override
    public void apply(UIToolBox toolBox) {
        Media media = toolBox.getData().getMedia();
        if (media.getStartTimestamp() != null ) {
            duplicateAnnotations.forEach(annotation -> {
                Duration elapsedTime = annotation.getElapsedTime();
                // Calculate timestamp from media start time and annotation elapsed time
                if (elapsedTime != null) {
                    Instant recordedDate = media.getStartTimestamp().plus(elapsedTime);
                    annotation.setRecordedTimestamp(recordedDate);
                }
            });

        }

        toolBox.getServices()
                .getAnnotationService()
                .createAnnotations(duplicateAnnotations)
                .thenAccept(annos -> {
                    // Replace our existing dups with the ones with the correct observation uuids
                    duplicateAnnotations.clear();
                    duplicateAnnotations.addAll(annos);
                    toolBox.getEventBus()
                            .send(new AnnotationsAddedEvent(annos));
                    if (selectAnnotations) {
                        toolBox.getEventBus()
                                .send(new AnnotationsSelectedEvent(annos));
                    }
                });
    }

    @Override
    public void unapply(UIToolBox toolBox) {
        final Collection<UUID> uuids = duplicateAnnotations.stream()
                .map(Annotation::getObservationUuid)
                .collect(Collectors.toList());

        toolBox.getServices()
                .getAnnotationService()
                .deleteAnnotations(uuids)
                .thenAccept(v ->
                   toolBox.getEventBus()
                        .send(new AnnotationsRemovedEvent(duplicateAnnotations)));
    }

    @Override
    public String getDescription() {
        return "Duplicate Annotations";
    }
}
