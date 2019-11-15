package org.mbari.vars.services;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.mbari.vars.services.model.Concept;
import org.mbari.vars.services.model.ConceptAssociationTemplate;
import org.mbari.vars.services.model.ConceptDetails;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Brian Schlining
 * @since 2019-11-14T15:55:00
 */
public class CachedConceptService2 implements ConceptService {
    private final ConceptService conceptService;
    private volatile String rootName;
    private volatile List<String> allNames = Collections.emptyList();
    private Map<String, Concept> conceptMap = new ConcurrentHashMap<>();
    private final AsyncLoadingCache<String, Optional<Concept>> conceptDetailsCache;
    private final AsyncLoadingCache<String, List<ConceptAssociationTemplate>> templateCache;



    public CachedConceptService2(ConceptService conceptService) {
        this.conceptService = conceptService;
        conceptDetailsCache = Caffeine.newBuilder()
                .expireAfterWrite(120, TimeUnit.MINUTES)
                .maximumSize(20000)
                .buildAsync((key, executor) -> loadConceptDetails(key));
        templateCache = Caffeine.newBuilder()
                .expireAfterWrite(120, TimeUnit.MINUTES)
                .maximumSize(20000)
                .buildAsync((key, executor) -> conceptService.findTemplates(key));
    }

    @Override
    public CompletableFuture<Concept> findRoot() {
        if (rootName == null) {
            return conceptService.findRoot()
                    .thenApply(c -> {
                        rootName = c.getName();
                        registerConcept(c);
                        Collections.sort(allNames);
                        return c;
                    });
        }
        else {
            return CompletableFuture.completedFuture(conceptMap.get(rootName));
        }
    }

    private void registerConcept(Concept concept) {
        conceptMap.put(concept.getName(), concept);
        allNames.add(concept.getName());
        if (concept.getAlternativeNames() != null && !concept.getAlternativeNames().isEmpty()) {
            allNames.addAll(concept.getAlternativeNames());
            concept.getAlternativeNames()
                    .forEach(name -> conceptMap.put(name, concept));
        }
        if (concept.getChildren() != null) {
            concept.getChildren().forEach(this::registerConcept);
        }
    }

    private CompletableFuture<Optional<Concept>> loadConceptDetails(String name) {
        return findRoot().thenCompose(root -> {
            if (!conceptMap.containsKey(name)) {
                return CompletableFuture.completedFuture(Optional.empty());
            }
            else {
                Concept concept = conceptMap.get(name);
                return conceptService.findDetails(concept.getName())
                        .thenApply(opt -> {
                            Optional<Concept> conceptOpt = Optional.of(concept);
                            opt.ifPresent(concept::setConceptDetails);
                            conceptDetailsCache.synchronous().put(name, conceptOpt);
                            if (concept.getAlternativeNames() != null) {
                                for (var n : concept.getAlternativeNames()) {
                                    conceptDetailsCache.synchronous().put(n, conceptOpt);
                                }
                            }
                            return conceptOpt;
                        });
            }
        });
    }

    @Override
    public CompletableFuture<Optional<ConceptDetails>> findDetails(String name) {
        return conceptDetailsCache.get(name)
                .thenApply(opt -> opt.map(Concept::getConceptDetails));
    }

    @Override
    public CompletableFuture<ConceptDetails> findRootDetails() {
        return findRoot().thenCompose(root -> findDetails(root.getName()).thenApply(Optional::get));
    }

    @Override
    public CompletableFuture<Optional<Concept>> findParent(String name) {
        return conceptService.findParent(name);
    }

    @Override
    public CompletableFuture<List<String>> findAllNames() {
        return findRoot().thenApply(root -> new ArrayList<>(allNames));
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
        return findRoot().thenCompose(root -> {
            if (conceptMap.containsKey(name)) {
                Concept concept = conceptMap.get(name);
                return findDetails(concept.getName())
                        .thenApply(cd -> Optional.of(concept));
            }
            else {
                return CompletableFuture.completedFuture(Optional.empty());
            }
        });
    }
}
