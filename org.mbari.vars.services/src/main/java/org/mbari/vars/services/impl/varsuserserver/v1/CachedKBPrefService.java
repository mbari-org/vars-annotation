package org.mbari.vars.services.impl.varsuserserver.v1;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.mbari.vars.services.PreferencesService;
import org.mbari.vars.services.model.PreferenceNode;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CachedKBPrefService implements PreferencesService {

    private record PrefKey(String name, String key) {
        public static PrefKey from(PreferenceNode node) {
            return new PrefKey(node.getKey(), node.getKey());
        }
    }
    private final Cache<PrefKey, PreferenceNode> cache;

    private interface PRequest {
        String name();
    }
    private record PRequestByName(String name) implements PRequest {}
    private record PRequestStartsWith(String name) implements PRequest{}
    private record PRequestWithKey(String name, String key)implements PRequest {}

    private final BlockingQueue<PRequest> pendingQueue = new LinkedBlockingQueue<>();

    private final Runnable runnable = () -> {
        while (true) {
            PRequest request = null;
            try {
                request = pendingQueue.poll(3600L, TimeUnit.SECONDS);
            }
            catch (InterruptedException e) {
                // TODO handle error via event
            }
            if (request != null) {

            }

        }
    };

    private final PreferencesService preferencesService;

    public CachedKBPrefService(PreferencesService preferencesService) {
        this.preferencesService = preferencesService;
        cache = Caffeine.newBuilder().maximumSize(200000).build();

        // TODO implement caching
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
        var prefNodes = cache.asMap()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().name().equals(nodeName))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        if (prefNodes.isEmpty()) {
            return preferencesService.findByName(nodeName)
                    .thenApply(nodes -> {
                        for (var n : nodes) {
                            var key = PrefKey.from(n);
                            cache.put(key, n);
                        }
                        return nodes;
                    });
        }
        else {
            return CompletableFuture.completedFuture(prefNodes);
        }

    }

    @Override
    public CompletableFuture<List<PreferenceNode>> findByNameLike(String nodeName) {
        var prefNodes = cache.asMap()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().name().startsWith(nodeName))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        if (prefNodes.isEmpty()) {
            return preferencesService.findByNameLike(nodeName)
                    .thenApply(nodes -> {
                        for (var n : nodes) {
                            var key = PrefKey.from(n);
                            cache.put(key, n);
                        }
                        return nodes;
                    });
        }
        else {
            return CompletableFuture.completedFuture(prefNodes);
        }

    }

    @Override
    public CompletableFuture<Optional<PreferenceNode>> findByNameAndKey(String nodeName, String key) {
        var pk = new PrefKey(nodeName, key);
        var node = cache.getIfPresent(pk);
        if (node == null) {
            return preferencesService.findByNameAndKey(nodeName, key)
                    .thenApply(opt -> {
                        opt.ifPresent(n -> {
                            var newPk = PrefKey.from(n);
                            cache.put(newPk, n);
                        });
                        return opt;
                    });
        }
        else {
            return CompletableFuture.completedFuture(Optional.of(node));
        }
    }

    public void clear() {
        cache.invalidateAll();
    }
}
