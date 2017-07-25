package org.mbari.m3.vars.annotation.commands;

import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.events.AnnotationsChangedEvent;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.services.AnnotationService;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.time.Timecode;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-07-20T22:23:00
 */
public class CopyAnnotationsCmd implements Command {

    private final UUID videoReferenceUuid;
    private final VideoIndex videoIndex;
    private final String observer;
    private final List<Annotation> originalAnnotations;
    private volatile List<Annotation> copies;

    public CopyAnnotationsCmd(UUID videoReferenceUuid,
                              VideoIndex videoIndex,
                              String observer,
                              List<Annotation> originalAnnotations) {
        this.videoReferenceUuid = videoReferenceUuid;
        this.videoIndex = videoIndex;
        this.observer = observer;
        this.originalAnnotations = originalAnnotations;
    }



    @Override
    public void apply(UIToolBox toolBox) {
        copies = originalAnnotations.stream()
                .map(Annotation::new) // Create copy
                .peek(a -> {           // Update fields
                    a.setVideoReferenceUuid(videoReferenceUuid);
                    a.setObservationUuid(null);
                    a.setImagedMomentUuid(null);
                    a.setObserver(observer);

                    Duration elapsedTime = videoIndex.getElapsedTime().orElse(null);
                    a.setElapsedTime(elapsedTime);

                    Timecode timecode = videoIndex.getTimecode().orElse(null);
                    a.setTimecode(timecode);

                    Instant timestamp = videoIndex.getTimestamp().orElse(null);
                    a.setRecordedTimestamp(timestamp);

                    a.setObservationTimestamp(Instant.now());
                })
                .collect(Collectors.toList());

        AnnotationService service = toolBox.getServices()
                .getAnnotationService();

        copies.stream()


        // TODO finish implemntati

    }

    @Override
    public void unapply(UIToolBox toolBox) {

    }

    @Override
    public String getDescription() {
        return null;
    }
}
