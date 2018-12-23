package org.mbari.m3.vars.annotation.services;

import org.mbari.m3.vars.annotation.model.PreferenceNode;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author Brian Schlining
 * @since 2017-06-08T16:25:00
 */
public interface PreferencesService {
    CompletableFuture<PreferenceNode> create(PreferenceNode node);
    CompletableFuture<Optional<PreferenceNode>> update(PreferenceNode node);
    CompletableFuture<Void> delete(PreferenceNode node);
    CompletableFuture<List<PreferenceNode>> findByName(String nodeName);
    CompletableFuture<List<PreferenceNode>> findByNameLike(String nodeName);
    CompletableFuture<Optional<PreferenceNode>> findByNameAndKey(String nodeName, String key);
}
