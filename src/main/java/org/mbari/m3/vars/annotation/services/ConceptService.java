package org.mbari.m3.vars.annotation.services;

import org.mbari.m3.vars.annotation.model.Concept;
import org.mbari.m3.vars.annotation.model.ConceptDetails;

import java.util.List;
import java.util.Optional;

/**
 * @author Brian Schlining
 * @since 2017-05-11T15:41:00
 */
public interface ConceptService {

    /**
     * Fetch all concepts and return the root node. You can walk the tree to find other nodes.
     * @return
     */
    Concept fetchConceptTree();

    Optional<ConceptDetails> findDetails(String name);

    List<String> fetchAllNames();


}
