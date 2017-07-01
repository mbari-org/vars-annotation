package org.mbari.m3.vars.annotation.model;

/**
 * @author Brian Schlining
 * @since 2017-05-11T16:55:00
 */
public class ConceptAssociationTemplate {
    private String linkName;
    private String toConcept;
    private String linkValue;

    public String getLinkName() {
        return linkName;
    }

    public String getToConcept() {
        return toConcept;
    }

    public String getLinkValue() {
        return linkValue;
    }
}
