package org.mbari.vars.services.model;

/**
 * @author Brian Schlining
 * @since 2019-03-08T15:35:00
 */
public class ConceptsRenamed {
    private String oldConcept;
    private String newConcept;
    private Integer count;

    public ConceptsRenamed(String oldConcept, String newConcept, Integer count) {
        this.oldConcept = oldConcept;
        this.newConcept = newConcept;
        this.count = count;
    }

    public String getOldConcept() {
        return oldConcept;
    }

    public String getNewConcept() {
        return newConcept;
    }

    public Integer getCount() {
        return count;
    }
}
