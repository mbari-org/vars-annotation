package org.mbari.m3.vars.annotation.util;

import org.mbari.m3.vars.annotation.model.PreferenceNode;
import org.mbari.m3.vars.annotation.services.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final long timeoutMillis;


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
        this.timeoutMillis = timeoutMillis;
    }

    @Override
    protected void putSpi(String key, String value) {
        log.debug("putSpi({}, {})", key, value);
        service.findByNameAndKey(absolutePath(), key)
                .thenCompose(opt -> {
                    if (opt.isPresent()) {
                        PreferenceNode node = opt.get();
                        if (!node.getValue().equals(value)) {
                            node.setValue(value);
                            service.update(node);
                        }
                    } else {
                        PreferenceNode node = new PreferenceNode(absolutePath(), key, value);
                        service.create(node);
                    }
                    return null;
                });
    }

    @Override
    protected String getSpi(String key) {
        log.debug("getSpi({})", key);
        try {
            Optional<PreferenceNode> opt = service.findByNameAndKey(absolutePath(), key)
                    .get(timeoutMillis, TimeUnit.MILLISECONDS);
            return opt.isPresent() ? opt.get().getValue() : null;
        } catch (Exception e) {
            log.warn("Failed to call getSpi(" + key + ")", e);
            return null;
        }
    }

    @Override
    protected void removeSpi(String key) {
        log.debug("removeSpi({})", key);
        service.findByNameAndKey(absolutePath(), key)
                .thenCompose(opt -> {
                    if (opt.isPresent()) {
                        PreferenceNode node = opt.get();
                        service.delete(node);
                    }
                    return null;
                });
    }

    @Override
    protected void removeNodeSpi() throws BackingStoreException {
        log.debug("removeNodeSpi()");
        service.findByNameLike(absolutePath())
                .thenCompose(nodes -> {
                    List<CompletableFuture<Void>> fs = nodes.stream()
                            .map(service::delete)
                            .collect(Collectors.toList());

                    CompletableFuture[] fa = fs.toArray(new CompletableFuture[fs.size()]);
                    CompletableFuture.allOf(fa);
                    return null;
                });
    }

    @Override
    protected String[] keysSpi() throws BackingStoreException {
        log.debug("keysSpi()");
        try {
            CompletableFuture<Stream<String>> f = service.findByNameLike(absolutePath())
                    .thenApply(nodes -> nodes.stream().map(PreferenceNode::getKey));
            return f.get(timeoutMillis, TimeUnit.MILLISECONDS)
                    .toArray(String[]::new);
        }
        catch (Exception e) {
            log.warn("Failed to lookup keys for node '" + absolutePath() + "'", e);
            return new String[0];
        }
    }

    @Override
    protected String[] childrenNamesSpi() throws BackingStoreException {
        log.debug("childrenNamesSpi()");
        String parentNodeName = absolutePath();
        CompletableFuture<List<PreferenceNode>> f = service.findByNameLike(parentNodeName);
        try {
            List<PreferenceNode> nodes = f.get(timeoutMillis, TimeUnit.MILLISECONDS);
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
                    .toArray(String[]::new);
        } catch (Exception e) {
            log.warn("Failed to look up child node names", e);
            return new String[0];
        }
    }

    @Override
    protected AbstractPreferences childSpi(String name) {
        log.debug("childSpi({})", name);
        return new WebPreferences(service, timeoutMillis, this, name);
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
