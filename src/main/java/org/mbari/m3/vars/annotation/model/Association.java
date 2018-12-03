package org.mbari.m3.vars.annotation.model;

import javax.swing.text.html.Option;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-05-11T13:29:00
 */
public class Association implements Cloneable, Details {
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

    /**
     * Create a standard text only Association from a generic details object
     * @param d
     * @return
     */
    public static Association fromDetails(Details d) {
        return new Association(d.getLinkName(), d.getToConcept(), d.getLinkValue());
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

    public static Optional<Association> parse(String s) {
        Optional<Association> a = Optional.empty();
        try {
            List<String> ss = Arrays.stream(s.split("[|]"))
                    .map(String::trim)
                    .collect(Collectors.toList());
            if (ss.size() == 3) {
                a = Optional.of(new Association(ss.get(0), ss.get(1), ss.get(2)));
            }
        }
        catch (Exception e) {
            // Do nothing
        }
        return a;
    }
}
