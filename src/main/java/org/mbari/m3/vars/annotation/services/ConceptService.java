package org.mbari.m3.vars.annotation.services;

import org.mbari.m3.vars.annotation.model.Concept;
import org.mbari.m3.vars.annotation.model.ConceptAssociationTemplate;
import org.mbari.m3.vars.annotation.model.ConceptDetails;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author Brian Schlining
 * @since 2017-05-11T15:41:00
 */
public interface ConceptService {

    /**
     * Fetch all concepts and return the root node. You can walk the tree to find other nodes.
     * @return
     */
    CompletableFuture<Concept> fetchConceptTree();

    /**
     * Retrieves details about a specific node, such as alternate names and media.
     * @param name The name of the node to search for, can be primary or other
     * @return The details for the Concept
     */
    CompletableFuture<Optional<ConceptDetails>> findDetails(String name);

    /**
     *
     * @return A list of all concept names found in the knowledgebase.
     */
    CompletableFuture<List<String>> findAllNames();

    CompletableFuture<List<ConceptAssociationTemplate>> findTemplates(String name);

}
