package org.mbari.vars.annotation.services.oni;

import org.mbari.vars.oni.sdk.r1.models.PreferenceNode;
import org.mbari.vars.oni.sdk.r1.PreferencesService;
import org.mbari.vars.annotation.etc.jdk.Loggers;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @author Brian Schlining
 * @since 2017-06-10T12:14:00
 */
public class WebPreferences extends AbstractPreferences {

    private final PreferencesService service;
    private final Loggers log = new Loggers(getClass());
    private final Duration timeout;


    /**
     *
     * @param service
     * @param timeoutMillis How long to wait for remote service calls to complete.
     *                      This value can be read using
     *                      config.getDuration("accounts.service.timeout").toMillis()
     * @param parent
     * @param name
     */
    public WebPreferences(PreferencesService service,
                          long timeoutMillis,
                          AbstractPreferences parent,
                          String name) {
        super(parent, name);
        this.service = service;
        this.timeout = Duration.ofMillis(timeoutMillis);
    }


    @Override
    protected void putSpi(String key, String value) {
        log.atDebug().log(() -> "putSpi(" + key + ", " + value + ")");
        // We have to sync all the async methods!! Use nodeFuture to sync
        CompletableFuture<PreferenceNode> nodeFuture = new CompletableFuture<>();
        service.findByNameAndKey(absolutePath(), key)
                .thenAccept(opt -> {
                    if (opt.isPresent()) {
                        PreferenceNode node = opt.get();
                        if (!node.getValue().equals(value)) {
                            node.setValue(value);
                            service.update(node)
                                .thenAccept(opt1 -> {
                                    if (opt1.isPresent()) {
                                        nodeFuture.complete(opt1.get());
                                    }
                                    else {
                                        throw new RuntimeException("Failed to update " + absolutePath() + "/" + key);
                                    }
                                });
                        }
                        else {
                            nodeFuture.complete(node);
                        }
                    } else {
                        PreferenceNode node = new PreferenceNode(absolutePath(), key, value);
                        service.create(node)
                            .thenAccept(nodeFuture::complete);
                    }
                });
        try {
            nodeFuture.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        }
        catch (Exception e) {
            log.atWarn().withCause(e).log("Failed to call putSpi(" + key + ", " + value + ")");
        }
    }

    @Override
    protected String getSpi(String key) {
        log.atDebug().log(() -> "getSpi(" + key + ")");
        try {
            Optional<PreferenceNode> opt = service.findByNameAndKey(absolutePath(), key)
                    .get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            return opt.map(PreferenceNode::getValue).orElse(null);
        } catch (Exception e) {
            log.atWarn().withCause(e).log("Failed to call getSpi(" + key + ")");
            return null;
        }
    }

    @Override
    protected void removeSpi(String key) {
        log.atDebug().log(() -> "removeSpi(" + key + ")");
        CompletableFuture<Void> doneFuture = new CompletableFuture<>();
        service.findByNameAndKey(absolutePath(), key)
                .thenAccept(opt -> {
                    if (opt.isPresent()) {
                        PreferenceNode node = opt.get();
                        service.delete(node)
                            .thenAccept(v -> doneFuture.complete(null)); // Async
                    }
                    else {
                        doneFuture.complete(null);
                    }
                });
        try {
            doneFuture.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        }
        catch (Exception e) {
            log.atWarn().withCause(e).log("Failed to call removeSpi(" + key + ")");
        }
    }

    @Override
    protected void removeNodeSpi() throws BackingStoreException {
        log.atDebug().log("removeNodeSpi()");
        // We need to make this sync. Use doneFuture to sync
        CompletableFuture<Void> doneFuture = new CompletableFuture<>();
        service.findByNameLike(absolutePath())
                .thenAccept(nodes -> {
                    List<CompletableFuture<Void>> fs = nodes.stream()
                            .map(service::delete)
                            .collect(Collectors.toList());

                    CompletableFuture[] fa = fs.toArray(new CompletableFuture[fs.size()]);
                    CompletableFuture.allOf(fa)
                        .thenAccept(v -> doneFuture.complete(null)); // Async
                });
        try {
            doneFuture.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        }
        catch (Exception e) {
            log.atWarn().withCause(e).log("Failed to call removeNodeSpi()");
        }
    }

    @Override
    protected String[] keysSpi() throws BackingStoreException {
        log.atDebug().log("keysSpi()");
        try {
            CompletableFuture<Stream<String>> f = service.findByNameLike(absolutePath())
                    .thenApply(nodes -> nodes.stream().map(PreferenceNode::getKey));
            return f.get(timeout.toMillis(), TimeUnit.MILLISECONDS)
                    .toArray(String[]::new);
        }
        catch (Exception e) {
            log.atWarn().withCause(e).log("Failed to lookup keys for node '" + absolutePath() + "'");
            return new String[0];
        }
    }

    @Override
    protected String[] childrenNamesSpi() throws BackingStoreException {
        log.atDebug().log("childrenNamesSpi()");
        String parentNodeName = absolutePath();
        CompletableFuture<List<PreferenceNode>> f = service.findByNameLike(parentNodeName);
        try {
            List<PreferenceNode> nodes = f.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            return nodes.stream()
                    .map(n -> {
                        String nodeName = n.getName();
                        // Strip off base path
                        String childPath = nodeName.substring(parentNodeName.length(),
                                nodeName.length());
                        // If path starts with '/' remove it
                        if (childPath.startsWith("/")) {
                            childPath = childPath.substring(1, childPath.length());
                        }
                        // Take up to the first '/' (or all if no slash is present)
                        if (childPath.contains("/")) {
                            childPath = childPath.substring(0, childPath.indexOf("/"));
                        }
                        return childPath;
                    })
                    .distinct()
                    .filter(s -> s != null && !s.isEmpty())
                    .toArray(String[]::new);
        } catch (Exception e) {
            log.atWarn().withCause(e).log("Failed to look up child node names");
            return new String[0];
        }
    }

    @Override
    protected AbstractPreferences childSpi(String name) {
        log.atDebug().log(() -> "childSpi(" + name + ")");
        return new WebPreferences(service, timeout.toMillis(), this, name);
    }


    @Override
    protected void syncSpi() throws BackingStoreException {
        // Do Nothing
    }

    @Override
    protected void flushSpi() throws BackingStoreException {
        // Do Nothing
    }
}
