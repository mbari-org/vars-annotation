package org.mbari.vars.services.model;

/**
 * {"concept":"Nanomia", "count":"55"}
 * 
 * @author Brian Schlining
 * @since 2019-03-08T14:52:00
 */
public class ConceptCount {
    private String concept;
    private Integer count;

    public ConceptCount(String concept, Integer count) {
        this.concept = concept;
        this.count = count;
    }

    public String getConcept() {
        return concept;
    }

    public Integer getCount() {
        return count;
    }
}
