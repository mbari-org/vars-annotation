package org.mbari.m3.vars.annotation.commands;

import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.AnnotationService;
import org.mbari.m3.vars.annotation.ui.AnnotationServiceDecorator;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.time.Timecode;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-10-23T15:29:00
 */
public class SetDurationCmd implements Command {
    private final List<Annotation> originalAnnotations;
    private volatile List<Annotation> modifiedAnnotations;


    public SetDurationCmd(List<Annotation> annotations) {
        this.originalAnnotations = annotations;
    }

    @Override
    public void apply(UIToolBox toolBox) {
        if (modifiedAnnotations == null) {
            modifiedAnnotations = originalAnnotations.stream()
                    .map(Annotation::new)
                    .collect(Collectors.toList());
        }
        toolBox.getMediaPlayer()
                .requestVideoIndex()
                .thenAccept(this::updateDuration)
                .thenAccept(v -> doAction(toolBox, modifiedAnnotations));
    }

    @Override
    public void unapply(UIToolBox toolBox) {
        doAction(toolBox, originalAnnotations);
    }

    private void updateDuration(VideoIndex videoIndex) {
        if (videoIndex.getTimestamp().isPresent()) {
            applyTimestamp(videoIndex.getTimestamp().get());
        }
        else if (videoIndex.getElapsedTime().isPresent()) {
            applyElapsedTime(videoIndex.getElapsedTime().get());
        }
        else if (videoIndex.getTimecode().isPresent()) {
            applyTimecode(videoIndex.getTimecode().get());
        }
    }

    private void doAction(UIToolBox toolBox, List<Annotation> annotations) {
        AnnotationService annotationService = toolBox.getServices().getAnnotationService();
        CompletableFuture[] futures = annotations.stream()
                .map(a -> annotationService.updateAnnotation(a))
                .toArray(i -> new CompletableFuture[i]);
        CompletableFuture.allOf(futures)
                .thenAccept(v -> {
                    AnnotationServiceDecorator asd = new AnnotationServiceDecorator(toolBox);
                    Set<UUID> uuids = annotations.stream()
                            .map(Annotation::getObservationUuid)
                            .collect(Collectors.toSet());
                    asd.refreshAnnotationsView(uuids);
                });
    }

    @Override
    public String getDescription() {
        return "Set duration for " +
                originalAnnotations.size() + " annotations";
    }

    private void applyElapsedTime(Duration elapsedTime) {
        modifiedAnnotations.forEach(a -> {
            Duration et = a.getElapsedTime();
            if (et != null) {
                Duration duration = elapsedTime.minus(et);
                a.setDuration(duration);
            }
        });
    }

    private void applyTimestamp(Instant timestamp) {
        modifiedAnnotations.forEach(a -> {
            Instant rt = a.getRecordedTimestamp();
            if (rt != null) {
                long millis = timestamp.toEpochMilli() - rt.toEpochMilli();
                Duration duration = Duration.ofMillis(millis);
                a.setDuration(duration);
            }
        });

    }

    private void applyTimecode(Timecode timecode) {
        if (timecode.isComplete()) {
            modifiedAnnotations.forEach(a -> {
                Timecode tc = a.getTimecode();
                if (tc != null && tc.isComplete()) {
                    long millis = Math.round((timecode.getSeconds() - tc.getSeconds()) * 1000L);
                    Duration duration = Duration.ofMillis(millis);
                    a.setDuration(duration);
                }
            });
        }
    }

}
