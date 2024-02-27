package org.mbari.vars.services.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.mbari.vars.services.model.Authorization;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class JwtHttpClient extends BaseHttpClient {

    private final AtomicReference<Authorization> authorization = new AtomicReference<>();
    private final String authHeaderKey;
    private final String authHeaderValue;
    private final Function<String, Authorization> bodyToAuthFn;


    public JwtHttpClient(HttpClient client,
                         URI authUri,
                         String authHeaderKey,
                         String authHeaderValue,
                         Function<String, Authorization> bodyToAuthFn) {
        super(client, authUri);
        this.authHeaderKey = authHeaderKey;
        this.authHeaderValue = authHeaderValue;
        this.bodyToAuthFn = bodyToAuthFn;
    }

    // --- Authorization
    private boolean isExpired(Authorization a) {
        try {
            DecodedJWT jwt = JWT.decode(a.getAccessToken());
            Instant iat = jwt.getExpiresAt().toInstant();
            return iat.isBefore(Instant.now());
        }
        catch (Exception e) {
            return true;
        }
    }

    public Authorization authorizeIfNeeded() {
        return authorization.updateAndGet(this::reauthorize);
    }

    protected Authorization reauthorize(Authorization a) {
        if ((a == null) || isExpired(a)) {
            return authorize().join();
        }
        return a;
    }


    public CompletableFuture<Authorization> authorize() {
        var request = HttpRequest.newBuilder()
                .uri(getBaseUri())
                .header(authHeaderKey, authHeaderValue) // .header("Authorization", "APIKEY " + apikey)
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        debugLog.logRequest(request, null);
        return submit(request, 200, bodyToAuthFn);
    }



}
