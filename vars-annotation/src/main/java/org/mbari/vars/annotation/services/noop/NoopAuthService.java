package org.mbari.vars.annotation.services.noop;


import org.mbari.vars.raziel.sdk.r1.ConfigurationService;
import org.mbari.vars.raziel.sdk.r1.models.BearerAuth;
import org.mbari.vars.raziel.sdk.r1.models.EndpointConfig;
import org.mbari.vars.raziel.sdk.r1.models.ServiceStatus;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Brian Schlining
 * @since 2017-05-24T10:21:00
 */
public class NoopAuthService implements ConfigurationService {

    @Override
    public CompletableFuture<BearerAuth> authenticate(String user, String password) {
        return CompletableFuture.failedFuture(new RuntimeException("NoopAuthService does not support authentication"));
    }

    @Override
    public CompletableFuture<List<EndpointConfig>> endpoints(String jwt) {
        return CompletableFuture.failedFuture(new RuntimeException("NoopAuthService does not support authentication"));
    }

    @Override
    public CompletableFuture<List<EndpointConfig>> endpoints() {
        return CompletableFuture.completedFuture(List.of());
    }

    @Override
    public CompletableFuture<List<ServiceStatus>> healthStatus() {
        return CompletableFuture.completedFuture(List.of());
    }
}

