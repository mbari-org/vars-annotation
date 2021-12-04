package org.mbari.vars.services;

import org.mbari.vars.services.model.EndpointConfig;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

public interface ConfigurationService {

    /**
     *
     * @param user
     * @param password
     * @return An authentication token (Bearer)
     */
    CompletableFuture<String> authenticate(URL baseUrl, String user, String password);

    CompletableFuture<EndpointConfig> endpoints(URL baseUrl, String jwt);

}
