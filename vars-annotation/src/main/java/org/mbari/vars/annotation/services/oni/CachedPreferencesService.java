package org.mbari.vars.annotation.services.oni;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.mbari.vars.oni.sdk.r1.PreferencesService;
import org.mbari.vars.oni.sdk.r1.models.PreferenceNode;


import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CachedPreferencesService implements PreferencesService {

    private record PrefKey(String name, String key) {
        public static PrefKey from(PreferenceNode node) {
            return new PrefKey(node.getName(), node.getKey());
        }
    }
    private final Cache<PrefKey, PreferenceNode> cache = Caffeine.newBuilder().maximumSize(200000).build();
    private final Object lock = new byte[]{};

    private final PreferencesService preferencesService;

    public CachedPreferencesService(PreferencesService preferencesService) {
        this.preferencesService = preferencesService;

    }

    @Override
    public CompletableFuture<PreferenceNode> create(PreferenceNode node) {
        return preferencesService.create(node)
                .thenApply(pn -> {
                    var key = PrefKey.from(pn);
                    cache.put(key, pn);
                    return pn;
                });
    }

    @Override
    public CompletableFuture<Optional<PreferenceNode>> update(PreferenceNode node) {
        return preferencesService.update(node)
                .thenApply(opt -> {
                    opt.ifPresent(pn -> {
                        var key = PrefKey.from(pn);
                        cache.put(key, pn);
                    });
                    return opt;
                });
    }

    @Override
    public CompletableFuture<Void> delete(PreferenceNode node) {
        return preferencesService.delete(node)
                .thenAccept(v -> cache.invalidate(PrefKey.from(node)));
    }

    @Override
    public CompletableFuture<List<PreferenceNode>> findByName(String nodeName) {
        return findByNameLike(nodeName)
                .thenApply(nodes -> nodes.stream()
                        .filter(pn -> pn.getName().equals(nodeName))
                        .collect(Collectors.toList()));

    }


    @Override
    public CompletableFuture<List<PreferenceNode>> findByNameLike(String nodeName) {
        return CompletableFuture.supplyAsync(() -> {
            List<PreferenceNode> prefNodes = Collections.emptyList();
            synchronized (lock) {
                prefNodes = cache.asMap()
                        .entrySet()
                        .stream()
                        .filter(entry -> entry.getKey().name().startsWith(nodeName))
                        .map(Map.Entry::getValue)
                        .collect(Collectors.toList());
                if (prefNodes.isEmpty()) {
                    try {
                        prefNodes = preferencesService.findByNameLike(nodeName)
                                .thenApply(nodes -> {
                                    for (var n : nodes) {
                                        var key = PrefKey.from(n);
                                        cache.put(key, n);
                                    }
                                    return nodes;
                                })
                                .get(10, TimeUnit.SECONDS);

                    } catch (Exception e) {

                    }
                }
            }
            return prefNodes;
        });

    }

    @Override
    public CompletableFuture<Optional<PreferenceNode>> findByNameAndKey(String nodeName, String key) {
        return findByNameLike(nodeName)
                .thenApply(nodes -> nodes.stream()
                        .filter(pn -> pn.getName().equals(nodeName) && pn.getKey().equals(key))
                        .findFirst());
    }

    public void clear() {
        cache.invalidateAll();
    }
}
