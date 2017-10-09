package org.mbari.m3.vars.annotation.model;

import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2017-05-11T13:29:00
 */
public class Association implements Cloneable {
    private UUID uuid;
    private String linkName;
    private String toConcept;
    private String linkValue;
    private String mimeType;

    public static final String VALUE_NIL = "nil";
    public static final String VALUE_SELF = "self";
    public static final Association NIL = new Association(VALUE_NIL, VALUE_NIL, VALUE_NIL);

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

    public Association(Association a) {
        uuid = a.uuid;
        linkName = a.linkName;
        toConcept = a.toConcept;
        linkValue = a.linkValue;
        mimeType = a.mimeType;
    }

    public Association(UUID uuid, Association a) {
        this.uuid = uuid;
        linkName = a.linkName;
        toConcept = a.toConcept;
        linkValue = a.linkValue;
        mimeType = a.mimeType;
    }

    public void resetUuid() {
        uuid = null;
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
