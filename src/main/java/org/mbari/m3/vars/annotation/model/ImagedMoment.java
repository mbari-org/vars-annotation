package org.mbari.m3.vars.annotation.model;

import org.mbari.vcr4j.time.Timecode;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Interface that captures the bare-essentials of both an Image and an Annotation.
 *
 * @author Brian Schlining
 * @since 2017-06-02T08:11:00
 */
public interface ImagedMoment {

    UUID getVideoReferenceUuid();
    UUID getImagedMomentUuid();
    Instant getRecordedTimestamp();
    Timecode getTimecode();
    Duration getElapsedTime();
}
