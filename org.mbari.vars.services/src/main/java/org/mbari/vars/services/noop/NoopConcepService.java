package org.mbari.vars.services.noop;



import org.mbari.vars.oni.sdk.r1.ConceptService;
import org.mbari.vars.oni.sdk.r1.models.Concept;
import org.mbari.vars.oni.sdk.r1.models.ConceptAssociationTemplate;
import org.mbari.vars.oni.sdk.r1.models.ConceptDetails;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class NoopConcepService implements ConceptService {

    private final Concept root = new Concept("root", null, Collections.emptyList(), Collections.emptyList());
    private final ConceptDetails rootDetails = new ConceptDetails("root", null, null,
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

    @Override
    public CompletableFuture<Concept> findRoot() {
        return CompletableFuture.completedFuture(root);
    }

    @Override
    public CompletableFuture<Optional<ConceptDetails>> findDetails(String name) {
        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    public CompletableFuture<ConceptDetails> findRootDetails() {
        return CompletableFuture.completedFuture(rootDetails);
    }

    @Override
    public CompletableFuture<Optional<Concept>> findParent(String name) {
        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    public CompletableFuture<List<String>> findAllNames() {
        return CompletableFuture.completedFuture(List.of("root"));
    }

    @Override
    public CompletableFuture<List<ConceptAssociationTemplate>> findAllTemplates() {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<ConceptAssociationTemplate>> findTemplates(String name) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<ConceptAssociationTemplate>> findTemplates(String name, String linkname) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<Optional<Concept>> findConcept(String name) {
        if (name.equals(root.getName())) {
            return CompletableFuture.completedFuture(Optional.of(root));
        }
        return CompletableFuture.completedFuture(Optional.empty());
    }
}
