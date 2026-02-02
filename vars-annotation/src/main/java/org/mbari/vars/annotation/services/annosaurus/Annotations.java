package org.mbari.vars.annotation.services.annosaurus;

import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;

import java.time.Duration;
import java.util.Optional;

public class Annotations {

    /**
     * Maps the elapsed time of an annotation to any media. If the annotation's
     * video reference UUID matches the media's UUID and the annotation has an elapsed time,
     * return the elapsed time. Otherwise, if the annotation has a recorded timestamp and
     * the media has a start timestamp, calculate the duration between the start timestamp
     * and the recorded timestamp.
     *
     * @param media The media to map the annotation's elapsed time to
     * @param annotation The annotation to map to the media
     * @return The elapsed time of the annotation on the media, or empty if not found
     */
    public static Optional<Duration> toMediaElapsedTime(Media media, Annotation annotation) {
        if (annotation.getVideoReferenceUuid().equals(media.getVideoReferenceUuid()) && annotation.getElapsedTime() != null) {
            return Optional.of(annotation.getElapsedTime());
        }
        else if (annotation.getRecordedTimestamp() != null && media.getStartTimestamp() != null) {
            return Optional.of(Duration.between(media.getStartTimestamp(), annotation.getRecordedTimestamp()));
        }
        return Optional.empty();
    }
}
