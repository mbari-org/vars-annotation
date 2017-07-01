package org.mbari.m3.vars.annotation.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-05-11T15:51:00
 */
public class Concept {
    private String name;
    private String rank;
    private List<Concept> children;
    private ConceptDetails conceptDetails;

    public Concept(String name, String rank, List<Concept> children) {
        this.name = name;
        this.rank = rank;
        this.children = Collections.unmodifiableList(children.stream()
                    .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                    .collect(Collectors.toList()));
    }

    public String getName() {
        return name;
    }

    public String getRank() {
        return rank;
    }

    public List<Concept> getChildren() {
        return children;
    }

    public ConceptDetails getConceptDetails() {
        return conceptDetails;
    }

    public void setConceptDetails(ConceptDetails conceptDetails) {
        this.conceptDetails = conceptDetails;
    }

    public List<String> flatten() {
        return flatten(this);
    }

    private static List<String> flatten(Concept concept) {
        List<String> accum = new ArrayList<>();
        flatten(concept, new ArrayList<>());
        accum.sort(String::compareToIgnoreCase);
        return accum;
    }

    private static void flatten(Concept concept, List<String> accum) {
        accum.add(concept.getName());
        concept.getChildren()
                .forEach(c -> flatten(concept, accum));
    }
}
