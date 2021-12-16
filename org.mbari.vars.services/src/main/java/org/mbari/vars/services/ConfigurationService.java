package org.mbari.vars.services;

import org.mbari.vars.services.model.Authorization;
import org.mbari.vars.services.model.EndpointConfig;
import org.mbari.vars.services.model.EndpointStatus;
import org.mbari.vars.services.model.HealthStatusCheck;

import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public interface ConfigurationService {

    /**
     *
     * @param user
     * @param password
     * @return An authentication token (Bearer)
     */
    CompletableFuture<Authorization> authenticate(URL baseUrl, String user, String password);

    CompletableFuture<List<EndpointConfig>> endpoints(URL baseUrl, String jwt);

    CompletableFuture<List<HealthStatusCheck>> healthStatus(URL baseUrl);

    default CompletableFuture<Set<EndpointStatus>> checkStatus(URL baseUrl, String user, String password) {
        return authenticate(baseUrl, user, password)
                .thenCompose(authorization ->
                    endpoints(baseUrl, authorization.getAccessToken())
                            .thenApply(EndpointConfig::decoratedNoWriteEndpoints)
                            .thenCompose(endpointConfigs ->
                                    healthStatus(baseUrl)
                                            .thenApply(healthStatusChecks -> EndpointStatus.collate(endpointConfigs, healthStatusChecks))
                            )
                );

    }

}
