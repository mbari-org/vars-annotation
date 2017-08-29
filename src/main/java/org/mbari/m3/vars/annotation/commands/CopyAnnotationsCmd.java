package org.mbari.m3.vars.annotation.commands;

import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.events.AnnotationsAddedEvent;
import org.mbari.m3.vars.annotation.events.AnnotationsChangedEvent;
import org.mbari.m3.vars.annotation.events.AnnotationsRemovedEvent;
import org.mbari.m3.vars.annotation.events.AnnotationsSelectedEvent;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.services.AnnotationService;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.time.Timecode;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
                              Collection<Annotation> originalAnnotations) {
        this.copiedAnnotations = originalAnnotations.stream()
                .map(a -> makeCopy(a, videoReferenceUuid, videoIndex, observer))
                .collect(Collectors.toList());
    }

    private Annotation makeCopy(Annotation annotation,
                                UUID videoReferenceUuid,
                                VideoIndex videoIndex,
                                String observer) {
        Annotation copy = new Annotation(annotation);
        copy.setVideoReferenceUuid(videoReferenceUuid);
        copy.setObservationUuid(null);
        copy.setImagedMomentUuid(null);
        copy.setObserver(observer);

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
