package org.mbari.m3.vars.annotation.services.annosaurus.v1;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.mbari.m3.vars.annotation.model.Authorization;

import java.io.IOException;

/**
 * @author Brian Schlining
 * @since 2017-05-23T15:45:00
 */
public class AuthInterceptor implements Interceptor {

    private final Authorization authorization;

    public AuthInterceptor(Authorization authorization) {
        this.authorization = authorization;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        Request request = original.newBuilder()
                .header("Authorization", authorization.toString())
                .method(original.method(), original.body())
                .build();
        return chain.proceed(request);
    }
}
