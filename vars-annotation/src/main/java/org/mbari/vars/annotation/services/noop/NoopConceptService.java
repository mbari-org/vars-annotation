package org.mbari.vars.annotation.services.noop;



import org.mbari.vars.oni.sdk.r1.ConceptService;
import org.mbari.vars.oni.sdk.r1.PreferencesService;
import org.mbari.vars.oni.sdk.r1.UserService;
import org.mbari.vars.oni.sdk.r1.models.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class NoopConceptService implements ConceptService, UserService, PreferencesService {

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

    @Override
    public CompletableFuture<Optional<Concept>> findPhylogenyDown(String s) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("NoopConceptService does not support findPhylogenyDown operation"));
    }

    @Override
    public CompletableFuture<PreferenceNode> create(PreferenceNode preferenceNode) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("NoopConceptService does not support create operation"));
    }

    @Override
    public CompletableFuture<Optional<PreferenceNode>> update(PreferenceNode preferenceNode) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("NoopConceptService does not support update operation"));
    }

    @Override
    public CompletableFuture<Void> delete(PreferenceNode preferenceNode) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("NoopConceptService does not support delete operation"));
    }

    @Override
    public CompletableFuture<List<PreferenceNode>> findByName(String s) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<PreferenceNode>> findByNameLike(String s) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<Optional<PreferenceNode>> findByNameAndKey(String s, String s1) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("NoopConceptService does not support findByNameAndKey operation"))  ;
    }

    @Override
    public CompletableFuture<List<User>> findAllUsers() {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<User> create(User user) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("NoopConceptService does not support create operation"));
    }

    @Override
    public CompletableFuture<Optional<User>> update(User user) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("NoopConceptService does not support update operation"));
    }

    @Override
    public CompletableFuture<Optional<User>> findByUsername(String s) {
        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    public CompletableFuture<Optional<User>> changePassword(String s, String s1, String s2) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("NoopConceptService does not support changePassword operation"));
    }
}
