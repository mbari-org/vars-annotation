package org.mbari.m3.vars.annotation.model;

import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.time.Timecode;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface that captures the bare-essentials of both an Image and an Annotation.
 *
 * @author Brian Schlining
 * @since 2017-06-02T08:11:00
 */
public interface ImagedMoment {

    UUID getVideoReferenceUuid();
    void setVideoReferenceUuid(UUID videoReferenceUuid);
    UUID getImagedMomentUuid();
    void setImagedMomentUuid(UUID imagedMomentUuid);
    Instant getRecordedTimestamp();
    void setRecordedTimestamp(Instant recordedTimestamp);
    Timecode getTimecode();
    void setTimecode(Timecode timecode);
    Duration getElapsedTime();
    void setElapsedTime(Duration elapsedTime);

    default VideoIndex toVideoIndex() {
        return new VideoIndex(Optional.ofNullable(getRecordedTimestamp()),
                Optional.ofNullable(getElapsedTime()),
                Optional.ofNullable(getTimecode()));
    }
}
