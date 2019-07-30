package org.mbari.vars.ui.commands;

import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.events.AnnotationsAddedEvent;
import org.mbari.vars.ui.events.AnnotationsRemovedEvent;
import org.mbari.vars.ui.events.AnnotationsSelectedEvent;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;
import org.mbari.vars.services.model.Media;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.time.Timecode;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-07-20T22:23:00
 */
public class CopyAnnotationsCmd implements Command {

    private final Collection<Annotation> copiedAnnotations;

    public CopyAnnotationsCmd(UUID videoReferenceUuid,
                              VideoIndex videoIndex,
                              String observer,
                              String activity,
                              Collection<Annotation> originalAnnotations) {
        this.copiedAnnotations = originalAnnotations.stream()
                .map(a -> makeCopy(a, videoReferenceUuid, videoIndex, observer, activity))
                .collect(Collectors.toList());
    }

    private Annotation makeCopy(Annotation annotation,
                                UUID videoReferenceUuid,
                                VideoIndex videoIndex,
                                String observer,
                                String activity) {
        Annotation copy = new Annotation(annotation);
        copy.setVideoReferenceUuid(videoReferenceUuid);
        copy.setObservationUuid(null);
        copy.setImagedMomentUuid(null);
        copy.setObserver(observer);
        copy.setActivity(activity);

        // if we don't null the associations uuid, it will fail to insert due to
        // a duplicate primary key clash.
        copy.getAssociations().forEach(Association::resetUuid);

        Duration elapsedTime = videoIndex.getElapsedTime().orElse(null);
        copy.setElapsedTime(elapsedTime);

        Timecode timecode = videoIndex.getTimecode().orElse(null);
        copy.setTimecode(timecode);

        Instant timestamp = videoIndex.getTimestamp().orElse(null);
        copy.setRecordedTimestamp(timestamp);

        copy.setObservationTimestamp(Instant.now());
        return copy;
    }



    @Override
    public void apply(UIToolBox toolBox) {
        Media media = toolBox.getData().getMedia();
        if (media.getStartTimestamp() != null ) {
            copiedAnnotations.forEach(annotation -> {
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
                .createAnnotations(copiedAnnotations)
                .thenAccept(annos -> {
                    copiedAnnotations.clear();
                    copiedAnnotations.addAll(annos);
                    toolBox.getEventBus()
                           .send(new AnnotationsAddedEvent(copiedAnnotations));
                    toolBox.getEventBus()
                           .send(new AnnotationsSelectedEvent(copiedAnnotations));
                });

    }

    @Override
    public void unapply(UIToolBox toolBox) {
        Collection<UUID> uuids = copiedAnnotations.stream()
                .map(Annotation::getObservationUuid)
                .collect(Collectors.toList());

        toolBox.getServices()
                .getAnnotationService()
                .deleteAnnotations(uuids)
                .thenAccept(v -> {
                    toolBox.getEventBus()
                            .send(new AnnotationsRemovedEvent(copiedAnnotations));
                    copiedAnnotations.forEach(a -> a.setImagedMomentUuid(null));
                });
    }

    @Override
    public String getDescription() {
        return null;
    }
}
