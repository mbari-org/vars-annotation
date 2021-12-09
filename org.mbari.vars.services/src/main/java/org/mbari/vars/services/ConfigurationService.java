package org.mbari.vars.services;

import org.mbari.vars.services.model.Authorization;
import org.mbari.vars.services.model.EndpointConfig;

import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ConfigurationService {

    /**
     *
     * @param user
     * @param password
     * @return An authentication token (Bearer)
     */
    CompletableFuture<Authorization> authenticate(URL baseUrl, String user, String password);

    CompletableFuture<List<EndpointConfig>> endpoints(URL baseUrl, String jwt);

}
