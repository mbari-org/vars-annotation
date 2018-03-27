package org.mbari.m3.vars.annotation.services;

import javafx.util.Pair;
import org.mbari.m3.vars.annotation.model.Concept;
import org.mbari.m3.vars.annotation.model.ConceptAssociationTemplate;
import org.mbari.m3.vars.annotation.model.ConceptDetails;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Caching implementation of a ConceptService. This one wraps another service. Initial calls
 * will be sent to the remote API while subsequent ones use the cache. If you need to force
 * reload, then call the `clear()` method.
 *
 * This class does not make any calls to the remote service itself, that is done by the
 * {@link ConceptService} that this class wraps.
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
    private ConceptDetails rootDetails;
    private Map<String, Concept> cache = new ConcurrentHashMap<>();
    private volatile List<String> allNames = Collections.emptyList();
    private volatile List<ConceptAssociationTemplate> allTemplates = Collections.emptyList();
    private final ConceptService conceptService;
    private final Map<String, List<ConceptAssociationTemplate>> cachedTemplates = new ConcurrentHashMap<>();

    /**
     *
     * @param conceptService The service that makes the actual calls
     */
    public CachedConceptService(ConceptService conceptService) {
        this.conceptService = conceptService;
        //findAllNames();
    }

    /**
     * Convienence method to load the main tree and start loading of details.
     *
     * @param  cachedConceptDetails Concept names in this list will have their templates pre-loaded
     * @return A future that completes when the conceptTree is loaded (but details
     * will continue to load in the background)
     */
    public CompletableFuture<Void> prefetch(List<String> cachedConceptDetails) {

        List<CompletableFuture<?>> futures = cachedConceptDetails.stream()
                .map(s -> new Pair<>(s, findTemplates(s)))
                .peek(p -> p.getValue()
                        .thenAccept(cats -> cachedTemplates.put(p.getKey(), cats)))
                .map(p -> p.getValue())
                .collect(Collectors.toList());

        CompletableFuture[] futureArray = futures.toArray(new CompletableFuture[futures.size()]);

        return CompletableFuture.allOf(futureArray);

    }

    @Override
    public CompletableFuture<Concept> findRoot() {
        CompletableFuture<Concept> f;
        if (root == null) {
            f = conceptService.findRoot()
                    .thenApply(c -> {
                        // Note that these are done in background, so the tree is
                        // returned while the details are still being fetched.
                        root = c;
                        addToCache(c);
                        return c;
                    });
        }
        else {
            f = CompletableFuture.completedFuture(root);
        }
        return f;
    }

    @Override
    public CompletableFuture<Optional<ConceptDetails>> findDetails(String name) {
        CompletableFuture<Optional<ConceptDetails>> f = CompletableFuture.completedFuture(Optional.empty());

        boolean fetchDetails = false;
        if (cache.containsKey(name)) {
            Optional<ConceptDetails> cd = Optional.ofNullable(cache.get(name).getConceptDetails());
            if (cd.isPresent()) {
                f = CompletableFuture.completedFuture(cd);
            }
            else {
                fetchDetails = true;
            }
        }

        if (fetchDetails) {
            CompletableFuture<Optional<ConceptDetails>> g = conceptService.findDetails(name);
            f = g.thenApply(opt -> {
                if (opt.isPresent() && cache.containsKey(name)) {
                    ConceptDetails cd = opt.get();
                    Concept c = cache.get(name);
                    c.setConceptDetails(cd);
                    cd.getAlternateNames()
                            .forEach(s -> cache.putIfAbsent(s, c));
                }
                return opt;
            });
        }

        return f;
    }

    @Override
    public CompletableFuture<ConceptDetails> findRootDetails() {
        CompletableFuture<ConceptDetails> f;
        if (rootDetails == null) {
            f = conceptService.findRootDetails()
                    .thenApply(cd -> rootDetails = cd);
        }
        else {
            f = CompletableFuture.completedFuture(rootDetails);
        }
        return f;
    }

    @Override
    public CompletableFuture<List<String>> findAllNames() {
        if (allNames.isEmpty()) {
            return conceptService.findAllNames()
                    .thenApply(ns -> {
                        allNames = Collections.unmodifiableList(ns);
                        return allNames;
                    });
        }
        else {
            return CompletableFuture.completedFuture(allNames);
        }
    }

    /**
     * We do not cache the linkTemplates, (too much data). Instead, we just pass through to the
     * underlying service.
     * @param name
     * @return
     */
    @Override
    public CompletableFuture<List<ConceptAssociationTemplate>> findTemplates(String name) {
        if (cachedTemplates.containsKey(name)) {
            return CompletableFuture.completedFuture(cachedTemplates.get(name));
        }
        else {
            return conceptService.findTemplates(name);
        }
    }

    @Override
    public CompletableFuture<List<ConceptAssociationTemplate>> findTemplates(String name,
                                                                             String linkname) {
        return conceptService.findTemplates(name, linkname);
    }

    @Override
    public CompletableFuture<Optional<Concept>> findConcept(String name) {
        if (cache.containsKey(name)) {
            return CompletableFuture.completedFuture(Optional.of(cache.get(name)));
        }
        else {
            CompletableFuture<Optional<Concept>> f = conceptService.findConcept(name);
            f.thenAccept(opt -> opt.ifPresent(this::addToCache));
            return f;
        }
    }

    private void addToCache(Concept concept) {
        if (concept != null) {
            cache.putIfAbsent(concept.getName(), concept);
            if (concept.getConceptDetails() == null) {
                findDetails(concept.getName());
            }
            else {
                concept.getConceptDetails()
                        .getAlternateNames()
                        .forEach(s -> cache.putIfAbsent(s, concept));
            }
            concept.getChildren()
                    .forEach(this::addToCache);
        }
    }

    @Override
    public CompletableFuture<List<ConceptAssociationTemplate>> findAllTemplates() {
        if (allTemplates.isEmpty()) {
            CompletableFuture<List<ConceptAssociationTemplate>> future = conceptService.findAllTemplates();
            future.thenAccept(cats -> {
                cats.sort(Comparator.comparing(ConceptAssociationTemplate::getLinkName));
                this.allTemplates = Collections.unmodifiableList(cats);
            });
            return future;
        }
        else {
            return CompletableFuture.completedFuture(allTemplates);
        }
    }

    public synchronized void clear() {
        cache.clear();
        cachedTemplates.clear();
        allNames = Collections.emptyList();
        root = null;
        rootDetails = null;
    }
}
