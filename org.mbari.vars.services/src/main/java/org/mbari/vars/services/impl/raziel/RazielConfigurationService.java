package org.mbari.vars.services.impl.raziel;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.mbari.vars.services.ConfigurationService;
import org.mbari.vars.services.model.EndpointConfig;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

public class RazielConfigurationService implements ConfigurationService {

    @Override
    public CompletableFuture<String> authenticate(URL baseUrl, String user, String password) {
        var url = new URL(baseUrl.toExternalForm() + "/auth");
        var credentials = user + ":" + password
        var encodedCredentials =
                new String(Base64.getEncoder().encode(credentials.getBytes(StandardCharsets.UTF_8)),
                        StandardCharsets.UTF_8);

        var client = new OkHttpClient();
        var request = new Request.Builder()
                .url(url)
                .addHeader("Authorizaton", "Basic " + encodedCredentials)
                .build();
        return null;
    }

    @Override
    public CompletableFuture<EndpointConfig> endpoints(URL baseUrl, String jwt) {
        return null;
    }
}
