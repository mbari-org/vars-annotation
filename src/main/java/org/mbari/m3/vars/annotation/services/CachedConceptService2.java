package org.mbari.m3.vars.annotation.services;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
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
 * @since 2018-02-14T14:45:00
 * @deprecated
 */
public class CachedConceptService2 implements ConceptService {

    private final ConceptService conceptService;
    private volatile String rootName;
    private volatile List<String> allNames = Collections.emptyList();

    private final AsyncLoadingCache<String, Optional<Concept>> conceptCache;
    private final AsyncLoadingCache<String, List<ConceptAssociationTemplate>> templateCache;


    public CachedConceptService2(ConceptService conceptService) {
        this.conceptService = conceptService;
        conceptCache = Caffeine.newBuilder()
                .expireAfterWrite(120, TimeUnit.MINUTES)
                .maximumSize(20000)
                .buildAsync((key, executor) -> loadConcept((String) key));
        templateCache = Caffeine.newBuilder()
                .expireAfterWrite(120, TimeUnit.MINUTES)
                .maximumSize(20000)
                .buildAsync((key, executor) -> conceptService.findTemplates(key));
        findRoot();
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
                        return c;
                    });
        }
        else {
            return conceptCache.get(rootName)
                    .thenApply(Optional::get);
        }
    }

    @Override
    public CompletableFuture<Optional<ConceptDetails>> findDetails(String name) {
        return conceptCache.get(name)
                .thenApply(opt -> opt.map(Concept::getConceptDetails));
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
