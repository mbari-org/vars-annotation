package org.mbari.m3.vars.annotation.model;

import java.util.List;
import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2019-06-05T16:39:00
 */
public class ConceptAssociationRequest {
    private String linkName;
    private List<UUID> videoReferenceUuids;

    public ConceptAssociationRequest(String linkName, List<UUID> videoReferenceUuids) {
        this.linkName = linkName;
        this.videoReferenceUuids = videoReferenceUuids;
    }

    public String getLinkName() {
        return linkName;
    }

    public List<UUID> getVideoReferenceUuids() {
        return videoReferenceUuids;
    }
}
