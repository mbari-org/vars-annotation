package org.mbari.m3.vars.annotation.model;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Data class for requesting annotations from multiple videos at the same time
 *
 * @author Brian Schlining
 * @since 2019-05-28T11:02:00
 */
public class ConcurrentRequest {

    private Instant startTimestamp;
    private Instant endTimestamp;
    private List<UUID> videoReferenceUuids;

    /**
     *
     * @param startTimestamp Filter out annotations before this date
     * @param endTimestamp Filter out annotations after this date
     * @param videoReferenceUuids A list of uuids for each video that we want
     *                            annotations from
     */
    public ConcurrentRequest(Instant startTimestamp, Instant endTimestamp, List<UUID> videoReferenceUuids) {
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.videoReferenceUuids = Collections.unmodifiableList(videoReferenceUuids);
    }

    public Instant getStartTimestamp() {
        return startTimestamp;
    }

    public Instant getEndTimestamp() {
        return endTimestamp;
    }

    public List<UUID> getVideoReferenceUuids() {
        return videoReferenceUuids;
    }
}
