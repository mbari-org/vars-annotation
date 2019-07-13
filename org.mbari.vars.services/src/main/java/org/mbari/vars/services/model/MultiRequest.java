package org.mbari.vars.services.model;

import java.util.List;
import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2019-07-10T16:26:00
 */
public class MultiRequest {

    private List<UUID> videoReferenceUuids;

    public MultiRequest(List<UUID> videoReferenceUuids) {
        this.videoReferenceUuids = videoReferenceUuids;
    }

    public List<UUID> getVideoReferenceUuids() {
        return videoReferenceUuids;
    }
}
