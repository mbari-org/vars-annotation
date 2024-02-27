package org.mbari.vars.services.impl.varsuserserver.v1;

import org.mbari.vars.services.PreferencesService;
import org.mbari.vars.services.impl.BaseHttpClient;
import org.mbari.vars.services.model.PreferenceNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class KBUserHttpClient extends BaseHttpClient implements PreferencesService {

    public KBUserHttpClient(HttpClient client, URI baseUri) {
        super(client, baseUri);
    }

    @Override
    public CompletableFuture<PreferenceNode> create(PreferenceNode node) {
        return null;
    }

    @Override
    public CompletableFuture<Optional<PreferenceNode>> update(PreferenceNode node) {
        return null;
    }

    @Override
    public CompletableFuture<Void> delete(PreferenceNode node) {
        return null;
    }

    @Override
    public CompletableFuture<List<PreferenceNode>> findByName(String nodeName) {
        return null;
    }

    @Override
    public CompletableFuture<List<PreferenceNode>> findByNameLike(String nodeName) {
        return null;
    }

    @Override
    public CompletableFuture<Optional<PreferenceNode>> findByNameAndKey(String nodeName, String key) {
        return null;
    }
}
