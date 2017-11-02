package org.mbari.m3.vars.annotation.services.varskbserver.v1;


import org.mbari.m3.vars.annotation.model.Concept;
import org.mbari.m3.vars.annotation.model.ConceptAssociationTemplate;
import org.mbari.m3.vars.annotation.model.ConceptDetails;
import org.mbari.m3.vars.annotation.services.ConceptService;
import org.mbari.m3.vars.annotation.services.RetrofitWebService;

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

    @Override
    public CompletableFuture<Optional<ConceptDetails>> findDetails(String name) {
        return sendRequest(service.findDetails(name)).thenApply(Optional::ofNullable);
    }

    public CompletableFuture<ConceptDetails> findRootDetails() {
        return sendRequest(service.findRootDetails());
    }

    @Override
    public CompletableFuture<List<String>> findAllNames() {
        return sendRequest(service.listConceptNames());
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
}
