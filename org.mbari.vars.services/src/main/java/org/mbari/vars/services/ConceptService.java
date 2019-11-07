package org.mbari.vars.services;

import org.mbari.vars.services.model.Concept;
import org.mbari.vars.services.model.ConceptAssociationTemplate;
import org.mbari.vars.services.model.ConceptDetails;

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
    CompletableFuture<Concept> findRoot();

    /**
     * Retrieves details about a specific node, such as alternate names and media.
     * @param name The name of the node to search for, can be primary or other
     * @return The details for the Concept
     */
    CompletableFuture<Optional<ConceptDetails>> findDetails(String name);

    CompletableFuture<ConceptDetails> findRootDetails();

    CompletableFuture<Optional<Concept>> findParent(String name);

    /**
     *
     * @return A list of all concept names found in the knowledgebase.
     */
    CompletableFuture<List<String>> findAllNames();

    CompletableFuture<List<ConceptAssociationTemplate>> findAllTemplates();

    CompletableFuture<List<ConceptAssociationTemplate>> findTemplates(String name);

    CompletableFuture<List<ConceptAssociationTemplate>> findTemplates(String name, String linkname);
    /**
     * Fetch a branch of the concept tree from the provided name on down
     * @param name
     * @return
     */
    CompletableFuture<Optional<Concept>> findConcept(String name);


}
