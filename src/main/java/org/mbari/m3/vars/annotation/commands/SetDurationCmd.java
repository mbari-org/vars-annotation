package org.mbari.m3.vars.annotation.commands;

import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.vcr4j.time.Timecode;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
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
                .thenAccept(videoIndex -> {
                   if (videoIndex.getTimestamp().isPresent()) {

                   }
                });

    }

    @Override
    public void unapply(UIToolBox toolBox) {

    }

    @Override
    public String getDescription() {
        return null;
    }

    private void applyElapsedTime(Duration elapsedTime) {

    }

    private void applyTimestamp(Instant timestamp) {

    }

    private void applyTimecode(Timecode timecode) {

    }

}
