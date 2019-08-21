package org.mbari.vars.services.model;

import org.mbari.vcr4j.time.Timecode;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2019-02-08T15:03:00
 */
public class Index {

    private UUID uuid;
    private UUID videoReferenceUuid;
    private Duration elapsedTime;
    private Instant recordedDate;
    private Timecode timecode;
    private Instant lastUpdatedTime;

    public Index() {
    }

    public Index(Index index) {
        this.uuid = index.uuid;
        this.videoReferenceUuid = index.videoReferenceUuid;
        this.elapsedTime = index.elapsedTime;
        this.recordedDate = index.recordedDate;
        this.timecode = index.timecode;
    }

    public Index(UUID uuid, UUID videoReferenceUuid, Duration elapsedTime) {
        this.uuid = uuid;
        this.videoReferenceUuid = videoReferenceUuid;
        this.elapsedTime = elapsedTime;
    }

    public Index(UUID uuid, UUID videoReferenceUuid, Instant recordedDate) {
        this.uuid = uuid;
        this.videoReferenceUuid = videoReferenceUuid;
        this.recordedDate = recordedDate;
    }

    public Index(UUID uuid, UUID videoReferenceUuid, Timecode timecode) {
        this.uuid = uuid;
        this.videoReferenceUuid = videoReferenceUuid;
        this.timecode = timecode;
    }

    public Index(UUID uuid, UUID videoReferenceUuid, Duration elapsedTime, Instant recordedDate, Timecode timecode) {
        this.uuid = uuid;
        this.videoReferenceUuid = videoReferenceUuid;
        this.elapsedTime = elapsedTime;
        this.recordedDate = recordedDate;
        this.timecode = timecode;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getVideoReferenceUuid() {
        return videoReferenceUuid;
    }

    public void setVideoReferenceUuid(UUID videoReferenceUuid) {
        this.videoReferenceUuid = videoReferenceUuid;
    }

    public Duration getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(Duration elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public Instant getRecordedDate() {
        return recordedDate;
    }

    public void setRecordedDate(Instant recordedDate) {
        this.recordedDate = recordedDate;
    }

    public Timecode getTimecode() {
        return timecode;
    }

    public void setTimecode(Timecode timecode) {
        this.timecode = timecode;
    }

    public Instant getLastUpdatedTime() {
        return lastUpdatedTime;
    }
}
