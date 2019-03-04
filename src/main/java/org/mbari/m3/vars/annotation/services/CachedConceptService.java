package org.mbari.m3.vars.annotation.services;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.mbari.m3.vars.annotation.model.Concept;
import org.mbari.m3.vars.annotation.model.ConceptAssociationTemplate;
import org.mbari.m3.vars.annotation.model.ConceptDetails;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Brian Schlining
 * @since 2018-03-06T15:13:00
 */
public class CachedConceptService implements ConceptService {

    private final ConceptService conceptService;
    private volatile String rootName;
    private volatile List<String> allNames = Collections.emptyList();

    private final AsyncLoadingCache<String, Optional<Concept>> conceptCache;
    private final AsyncLoadingCache<String, List<ConceptAssociationTemplate>> templateCache;



    public CachedConceptService(ConceptService conceptService) {
        this.conceptService = conceptService;

        conceptCache = Caffeine.newBuilder()
                .expireAfterWrite(120, TimeUnit.MINUTES)
                .maximumSize(20000)
                .buildAsync((key, executor) -> loadConcept(key));
        templateCache = Caffeine.newBuilder()
                .expireAfterWrite(120, TimeUnit.MINUTES)
                .maximumSize(20000)
                .buildAsync((key, executor) -> conceptService.findTemplates(key));
        findRoot();
    }

    public synchronized void clear() {
        rootName = null;
        allNames.clear();
        conceptCache.synchronous().invalidateAll();
        templateCache.synchronous().invalidateAll();
    }

    private CompletableFuture<Optional<Concept>> loadConcept(String name) {
        return conceptService.findConcept(name)
                .thenApply(opt ->  {
                    System.out.println("Loaded " + name);
                    opt.ifPresent(this::loadConceptDetails);
                    return opt;
                });
    }

    private CompletableFuture<Optional<Concept>> loadConceptDetails(Concept c) {
        return conceptService.findDetails(c.getName()).thenApply(opt -> {
            opt.ifPresent(cd -> {
                c.setConceptDetails(cd);
                for (String name : cd.getAlternateNames()) {
                    conceptCache.synchronous().put(name, Optional.of(c));
                }
            });
            return Optional.of(c);
        });
    }

    @Override
    public CompletableFuture<Concept> findRoot() {
        if (rootName == null) {
            return conceptService.findRoot()
                    .thenApply(c -> {
                        rootName = c.getName();
                        conceptCache.put(rootName, loadConceptDetails(c));
                        cacheChildren(c);
                        return c;
                    });
        }
        else {
            return conceptCache.get(rootName)
                    .thenApply(Optional::get);
        }
    }

    private void cacheChildren(Concept concept) {
        concept.getChildren()
                .forEach(child -> {
                    Optional<Concept> opt = Optional.of(child);
                    LoadingCache<String, Optional<Concept>> syncCache = conceptCache.synchronous();
                    syncCache.put(child.getName(), opt);
                    child.getAlternativeNames()
                            .forEach(alternateName -> syncCache.put(alternateName, opt));
                    cacheChildren(child);
                });
    }

    @Override
    public CompletableFuture<Optional<ConceptDetails>> findDetails(String name) {
        CompletableFuture<Optional<ConceptDetails>> f = new CompletableFuture<>();
        conceptCache.get(name)
                .thenAccept(opt -> {
                    if (opt.isPresent()) {
                        Concept concept = opt.get();
                        if (concept.getConceptDetails() == null) {
                            loadConceptDetails(concept)
                                    .thenAccept(opt2 -> {
                                        if (opt2.isPresent()) {
                                            f.complete(Optional.ofNullable(opt2.get().getConceptDetails()));
                                        }
                                        else {
                                            f.complete(Optional.empty());
                                        }
                                    });
                        } else {
                            f.complete(Optional.of(concept.getConceptDetails()));
                        }
                    }
                    else {
                        f.complete(Optional.empty());
                    }
                });
        return f;
    }

    @Override
    public CompletableFuture<ConceptDetails> findRootDetails() {
        if (rootName == null) {
            CompletableFuture<ConceptDetails> f = new CompletableFuture<>();
            f.completeExceptionally(new RuntimeException("Root has not yet been loadedd"));
            return f;
        }
        else {
            return findDetails(rootName).thenApply(Optional::get);
        }
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

    @Override
    public CompletableFuture<List<ConceptAssociationTemplate>> findAllTemplates() {
        return conceptService.findAllTemplates();
    }

    @Override
    public CompletableFuture<List<ConceptAssociationTemplate>> findTemplates(String name) {
        return templateCache.get(name);
    }

    @Override
    public CompletableFuture<List<ConceptAssociationTemplate>> findTemplates(String name, String linkname) {
        return conceptService.findTemplates(name, linkname);
    }

    @Override
    public CompletableFuture<Optional<Concept>> findConcept(String name) {
        return conceptCache.get(name);
    }

}
