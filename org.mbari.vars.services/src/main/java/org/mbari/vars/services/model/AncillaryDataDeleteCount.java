package org.mbari.vars.services.model;

import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2018-07-05T15:30:00
 */
public class AncillaryDataDeleteCount {
    UUID videoReferenceUuid;
    Integer count;

    public AncillaryDataDeleteCount(UUID videoReferenceUuid, Integer count) {
        this.videoReferenceUuid = videoReferenceUuid;
        this.count = count;
    }

    public UUID getVideoReferenceUuid() {
        return videoReferenceUuid;
    }

    public Integer getCount() {
        return count;
    }

}
