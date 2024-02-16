package org.mbari.vars.services;

import org.slf4j.LoggerFactory;
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
                LoggerFactory.getLogger(getClass())
                        .warn("Exception thrown when making a REST call", throwable);
                f.completeExceptionally(throwable);
            }
        });
        return f;
    }

    default <T> CompletableFuture<T> sendRequestNoFail(Call<T> call) {
        CompletableFuture<T> f = new CompletableFuture<>();
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                f.complete(response.body());
            }

            @Override
            public void onFailure(Call<T> call, Throwable throwable) {
                LoggerFactory.getLogger(getClass())
                        .warn("Exception thrown when making a REST call", throwable);
//                f.completeExceptionally(throwable);
                f.complete(null); // TODO: 2024-02-02 Brian Schlining - This is a hack to get around the fact that the
                //  CompletableFuture is not being completed when the server returns a 404. This is a bug in the
                //  retrofit library.
            }
        });
        return f;
    }


    default <T> CompletableFuture<Boolean> sendNoContentRequest(Call<T> call) {
        CompletableFuture<Boolean> f = new CompletableFuture<>();
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                f.complete(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<T> call, Throwable throwable) {
                LoggerFactory.getLogger(getClass())
                        .warn("Exception thrown when making a REST call", throwable);
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
