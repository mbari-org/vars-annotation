package org.mbari.m3.vars.annotation.commands;

import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Media;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Move annotations to a different Media
 * @author Brian Schlining
 * @since 2017-07-20T22:21:00
 */
public class MoveAnnotationsCmd extends UpdateAnnotationsCmd {

    private final Media media;

    public MoveAnnotationsCmd(List<Annotation> originalAnnotations, Media media) {
        super(originalAnnotations, originalAnnotations.stream()
            .map(Annotation::new)
            .peek(a -> {
                if (!a.getVideoReferenceUuid().equals(media.getVideoReferenceUuid())) {
                    a.setVideoReferenceUuid(media.getVideoReferenceUuid());
                    // Update recordedTimestamp to match the new media.
                    if (a.getElapsedTime() != null && media.getStartTimestamp() != null) {
                        Instant recordedTimestamp = media.getStartTimestamp().plus(a.getElapsedTime());
                        a.setRecordedTimestamp(recordedTimestamp);
                    } else {
                        a.setRecordedTimestamp(null);
                    }
                }
            })
            .collect(Collectors.toList()));
        this.media = media;
    }

    @Override
    public String getDescription() {
        return "Moving " + originalAnnotations.size() + " to " + media.getVideoName();
    }
}
