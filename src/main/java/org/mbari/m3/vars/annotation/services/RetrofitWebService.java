package org.mbari.m3.vars.annotation.services;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.concurrent.CompletableFuture;

/**
 * @author Brian Schlining
 * @since 2017-05-27T11:55:00
 */
public interface RetrofitWebService {

    /**
     * Wraps a retrofit call with a CompletableFuture and does an async request using
     * the Call object. This keeps us from polluting the code base with retrofit specific
     * code.
     * @param call
     * @param <T>
     * @return
     */
    default <T> CompletableFuture<T> sendRequest(Call<T> call) {
        CompletableFuture<T> f = new CompletableFuture<>();
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                f.complete(response.body());
            }

            @Override
            public void onFailure(Call<T> call, Throwable throwable) {
                f.completeExceptionally(throwable);
            }
        });
        return f;
    }

    /**
     * Convert an object to it's string form or null if the object is null
     * @param obj
     * @return
     */
    default String asString(Object obj) {
        return (obj == null) ? null : obj.toString();
    }


}
