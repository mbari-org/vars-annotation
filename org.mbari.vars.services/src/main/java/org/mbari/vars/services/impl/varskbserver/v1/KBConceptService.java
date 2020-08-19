package org.mbari.vars.services.impl.varskbserver.v1;


import org.mbari.vars.services.model.Concept;
import org.mbari.vars.services.model.ConceptAssociationTemplate;
import org.mbari.vars.services.model.ConceptDetails;
import org.mbari.vars.services.ConceptService;
import org.mbari.vars.services.RetrofitWebService;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service that calls the REST API for vampire-squid. This version does NO caching,
 * each call will be sent to the server.
 *
 * @author Brian Schlining
 * @since 2017-05-11T16:13:00
 */
public class KBConceptService implements ConceptService, RetrofitWebService {


    /** Underlying retrofit API service */
    private final KBWebService service;


    @Inject
    public KBConceptService(KBWebServiceFactory serviceFactory) {
        service = serviceFactory.create(KBWebService.class);
    }


    @Override
    public CompletableFuture<Concept> findRoot() {
        return sendRequest(service.findRootDetails())
                .thenCompose(root -> sendRequest(service.findTree(root.getName())));
    }

    public CompletableFuture<Optional<Concept>> findParent(String name) {
        return sendRequest(service.findParentBranch(name))
                .thenApply(c -> findParent(c, name));
    }

    @Override
    public CompletableFuture<Optional<ConceptDetails>> findDetails(String name) {
        return sendRequest(service.findDetails(name)).thenApply(Optional::ofNullable);
    }

    public CompletableFuture<ConceptDetails> findRootDetails() {
        return sendRequest(service.findRootDetails());
    }

    @Override
    public CompletableFuture<List<String>> findAllNames() {
        return sendRequest(service.findAllNames());
    }

    @Override
    public CompletableFuture<List<ConceptAssociationTemplate>> findTemplates(String name) {
        return sendRequest(service.findTemplates(name));
    }

    @Override
    public CompletableFuture<List<ConceptAssociationTemplate>> findTemplates(String name,
                                                                             String linkname) {
        return sendRequest(service.findTemplates(name, linkname));
    }

    @Override
    public CompletableFuture<Optional<Concept>> findConcept(String name) {
        return sendRequest(service.findTree(name)).thenApply(Optional::ofNullable);
    }

    @Override
    public CompletableFuture<List<ConceptAssociationTemplate>> findAllTemplates() {
        return sendRequest(service.findAllTemplates());
    }

    /**
     * Take a branch from `phylogeny/down` and finds the parent concept for given
     * concept name
     * @param concept The root or node of a branch returned from `phylogeny/down`
     *                call
     * @param conceptName The concept whose parent we want to find.
     * @return The parent concept if found. Empty otherwise
     */
    private Optional<Concept> findParent(Concept concept, String conceptName) {

        Optional<Concept> theParent = Optional.empty();

        if (concept != null) {
            var parent = concept;
            while (true) {
                if (parent.getChildren().isEmpty()) {
                    break;
                }
                var match = parent.getChildren()
                        .stream()
                        .map(Concept::getName)
                        .filter(c -> c.equalsIgnoreCase(conceptName))
                        .findAny();
                if (match.isPresent()) {
                    theParent = Optional.of(parent);
                    break;
                } else {
                    parent = parent.getChildren().get(0);
                }
            }
        }
        return theParent;
    }
}
