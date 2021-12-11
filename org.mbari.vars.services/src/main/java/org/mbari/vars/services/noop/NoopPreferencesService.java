package org.mbari.vars.services.noop;

import org.mbari.vars.services.PreferencesService;
import org.mbari.vars.services.model.PreferenceNode;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class NoopPreferencesService implements PreferencesService {
    @Override
    public CompletableFuture<PreferenceNode> create(PreferenceNode node) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Optional<PreferenceNode>> update(PreferenceNode node) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Void> delete(PreferenceNode node) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<List<PreferenceNode>> findByName(String nodeName) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<PreferenceNode>> findByNameLike(String nodeName) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<Optional<PreferenceNode>> findByNameAndKey(String nodeName, String key) {
        return CompletableFuture.completedFuture(Optional.empty());
    }
}
