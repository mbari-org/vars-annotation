package org.mbari.m3.vars.annotation.model;

import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2019-06-05T16:40:00
 */
public class ConceptAssociation {

    private UUID uuid;
    private UUID videoReferenceUuid;
    private String concept;
    private String linkName;
    private String toConcept;
    private String linkValue;
    private String mimeType;

    public ConceptAssociation(UUID uuid, UUID videoReferenceUuid,
                              String concept, String linkName, String toConcept,
                              String linkValue, String mimeType) {
        this.uuid = uuid;
        this.videoReferenceUuid = videoReferenceUuid;
        this.concept = concept;
        this.linkName = linkName;
        this.toConcept = toConcept;
        this.linkValue = linkValue;
        this.mimeType = mimeType;
    }

    public UUID getUuid() {
        return uuid;
    }

    public UUID getVideoReferenceUuid() {
        return videoReferenceUuid;
    }

    public String getConcept() {
        return concept;
    }

    public String getLinkName() {
        return linkName;
    }

    public String getToConcept() {
        return toConcept;
    }

    public String getLinkValue() {
        return linkValue;
    }

    public String getMimeType() {
        return mimeType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConceptAssociation that = (ConceptAssociation) o;

        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    public Association asAssociation() {
        return new Association(linkName, toConcept, linkValue, mimeType, uuid);
    }
}
