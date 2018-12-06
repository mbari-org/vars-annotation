package org.mbari.m3.vars.annotation.model;

import javax.annotation.Nonnull;

/**
 * @author Brian Schlining
 * @since 2017-05-11T16:55:00
 */
public class ConceptAssociationTemplate implements Details {
    private String linkName;
    private String toConcept;
    private String linkValue;

    public static final ConceptAssociationTemplate NIL = new ConceptAssociationTemplate(Association.VALUE_NIL,
            Association.VALUE_NIL, Association.VALUE_NIL);

    public ConceptAssociationTemplate(@Nonnull String linkName, @Nonnull String toConcept, @Nonnull String linkValue) {
        this.linkName = linkName;
        this.toConcept = toConcept;
        this.linkValue = linkValue;
    }

    public ConceptAssociationTemplate(@Nonnull Details details) {
        this(details.getLinkName(), details.getToConcept(), details.getLinkValue());
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

    @Override
    public String toString() {
        String s = linkName + " | "  + toConcept + " | " + linkValue;
        if (s.length() > 80) {
            s = s.substring(0, 80) + "...";
        }
        return s;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConceptAssociationTemplate that = (ConceptAssociationTemplate) o;

        if (!linkName.equals(that.linkName)) return false;
        if (!toConcept.equals(that.toConcept)) return false;
        return linkValue.equals(that.linkValue);
    }

    @Override
    public int hashCode() {
        int result = linkName.hashCode();
        result = 31 * result + toConcept.hashCode();
        result = 31 * result + linkValue.hashCode();
        return result;
    }
}
