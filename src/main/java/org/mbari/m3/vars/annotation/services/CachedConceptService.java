package org.mbari.m3.vars.annotation.services;

import org.mbari.m3.vars.annotation.model.Concept;
import org.mbari.m3.vars.annotation.model.ConceptDetails;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caching implementation of a ConceptService. This one wraps another service. Initial calls
 * will be sent to the remote API while subsequent ones use the cache. If you need to force
 * reload, then call the `clear()` method.
 *
 * Usage:
 * <pre>
 *     ConceptService cs = // initialize your base service.
 *     ConceptService cachedService = new CachedConceptService(cs);
 * </pre>
 * @author Brian Schlining
 * @since 2017-05-15T14:42:00
 */
public class CachedConceptService implements ConceptService {

    private Concept root;
    private Map<String, Concept> cache = new ConcurrentHashMap<>();
    private List<String> allNames = Collections.emptyList();
    private final ConceptService conceptService;

    public CachedConceptService(ConceptService conceptService) {
        this.conceptService = conceptService;
    }


    @Override
    public CompletableFuture<Concept> fetchConceptTree() {
        CompletableFuture<Concept> f;
        if (root == null) {
            f = conceptService.fetchConceptTree()
                    .thenApply(c -> {
                        addToCache(root);
                        return root;
                    })
                    .thenApply(c -> {
                        root = c;
                        return root;
                    });
        }
        else {
            f = CompletableFuture.completedFuture(root);
        }
        return f;
    }

    @Override
    public CompletableFuture<Optional<ConceptDetails>> findDetails(String name) {
        CompletableFuture<Optional<ConceptDetails>> f;
        if (cache.containsKey(name)) {
            Optional<ConceptDetails> cd = Optional.of(cache.get(name).getConceptDetails());
            f = CompletableFuture.completedFuture(cd);
        }
        else {
            CompletableFuture<Optional<ConceptDetails>> g = conceptService.findDetails(name);
            f = g.thenApply(cd -> {
                if (cd.isPresent() && cache.containsKey(name)) {
                    Concept c = cache.get(name);
                    c.setConceptDetails(cd.get());
                }
                return cd;
            });
        }
        return f;
    }

    @Override
    public CompletableFuture<List<String>> findAllNames() {
        if (allNames.isEmpty()) {
            return conceptService.findAllNames()
                    .thenApply(ns -> {
                        allNames = ns;
                        return allNames;
                    });
        }
        else {
            return CompletableFuture.completedFuture(allNames);
        }
    }

    private void addToCache(Concept concept) {
        cache.putIfAbsent(concept.getName(), concept);
        if (concept.getConceptDetails() != null) {
            concept.getConceptDetails()
                    .getAlternateNames()
                    .forEach(s -> cache.putIfAbsent(s, concept));
        }
        concept.getChildren()
                .forEach(this::addToCache);
    }

    public synchronized void clear() {
        cache.clear();
        allNames = Collections.emptyList();
        root = null;
    }
}
