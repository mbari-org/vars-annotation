package org.mbari.m3.vars.annotation.model;

import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2017-05-11T13:29:00
 */
public class Association {
    private UUID uuid;
    private String linkName;
    private String toConcept;
    private String linkValue;
    private String mimeType;

    public Association(String linkName, String toConcept, String linkValue, String mimeType) {
        this.linkName = linkName;
        this.toConcept = toConcept;
        this.linkValue = linkValue;
        this.mimeType = mimeType;
    }

    public Association(String linkName, String toConcept, String linkValue) {
        this.linkName = linkName;
        this.toConcept = toConcept;
        this.linkValue = linkValue;
        this.mimeType = "text/plain";
    }

    public Association(String linkName, String toConcept, String linkValue, String mimeType, UUID uuid) {
        this.linkName = linkName;
        this.toConcept = toConcept;
        this.linkValue = linkValue;
        this.mimeType = mimeType;
        this.uuid = uuid;
    }


    public UUID getUuid() {
        return uuid;
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
    public String toString() {
        return linkName + " | "  + toConcept + " | " + linkValue;
    }
}
