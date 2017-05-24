package org.mbari.m3.vars.annotation.services.annosaurus.v1;

import com.auth0.jwt.JWT;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.mbari.m3.vars.annotation.model.Authorization;
import org.mbari.m3.vars.annotation.services.AuthService;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Brian Schlining
 * @since 2017-05-23T15:45:00
 */
public class AuthInterceptor implements Interceptor {

    private final AtomicReference<Authorization> authorization = new AtomicReference<>();

    private final AuthService authService;

    public AuthInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        Authorization a = authorization.updateAndGet(this::reauthorize);
        Request request = original.newBuilder()
                .header("Authorization", a.toString())
                .method(original.method(), original.body())
                .build();
        return chain.proceed(request);
    }

    private Authorization reauthorize(Authorization a) {
        if ((a == null) || isExpired(a)) {
            a = authService.authorize()
                    .orElseGet(null);
        }
        return a;
    }

    private boolean isExpired(Authorization a) {
        JWT jwt = JWT.decode(a.getAccessToken());
        Instant iat = jwt.getExpiresAt().toInstant();
        return iat.isBefore(Instant.now());
    }
}
