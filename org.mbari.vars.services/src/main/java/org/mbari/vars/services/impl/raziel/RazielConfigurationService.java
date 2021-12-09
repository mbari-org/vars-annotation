package org.mbari.vars.services.impl.raziel;

import com.google.gson.Gson;
import okhttp3.*;
import org.mbari.vars.services.ConfigurationService;
import org.mbari.vars.services.model.Authorization;
import org.mbari.vars.services.model.EndpointConfig;

import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RazielConfigurationService implements ConfigurationService {

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    @Override
    public CompletableFuture<Authorization> authenticate(URL baseUrl, String username, String password) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var url = URI.create(baseUrl.toExternalForm() + "/auth").toURL();
                var request = new Request.Builder()
                        .url(url)
                        .addHeader("Authorizaton", Credentials.basic(username, password))
                        .addHeader("Accept", "application/json")
                        .post(RequestBody.create(MediaType.parse("text/plain"), ""))
                        .build();
                try (var response = client.newCall(request).execute()) {
                    var body = response.body().string();
                    return gson.fromJson(body, Authorization.class);
                }
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<List<EndpointConfig>> endpoints(URL baseUrl, String jwt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var url = URI.create(baseUrl.toExternalForm() + "/endpoints").toURL();
                var request = new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer " + jwt)
                        .get()
                        .build();
                try (var response = client.newCall(request).execute()) {
                    var body = response.body().string();
                    var array =  gson.fromJson(body, EndpointConfig[].class);
                    return Arrays.asList(array);
                }
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
