package org.mbari.vars.services.impl;

import com.github.mizosoft.methanol.Methanol;
import org.mbari.vars.core.util.Logging;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class BaseHttpClient {

    protected final Logging log = new Logging(getClass());
    protected final Logging debugLog = log.atDebug();
    private final HttpClient client;
    private final URI baseUri;
    protected final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public BaseHttpClient(HttpClient client, URI baseUri) {
        this.client = client;
        this.baseUri = baseUri;
    }

    public URI buildUri(String path) {
        var newPath = baseUri.getPath() + path;
        return baseUri.resolve(newPath);
    }

    public static HttpClient newHttpClient(Duration timeout) {
        return Methanol.newBuilder()
                .autoAcceptEncoding(true)
                .connectTimeout(timeout)
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .readTimeout(timeout)
                .requestTimeout(timeout)
                .userAgent("org.mbari.vars.services")
                .build();
    }

    public HttpClient getHttpClient() {
        return client;
    }

    public URI getBaseUri() {
        return baseUri;
    }

    /**
     * Handle a request
     * @param request The resuest to send
     * @param okCode The expected code if completed successfully
     * @param fn A function to handle the response body. If null then the body is ignored
     * @return A CompletableFuture that will complete when the request is done
     * @param <T> The type that the response body will be converted to
     */
    public <T> CompletableFuture<T> submit(HttpRequest request,
                                           int okCode,
                                           Function<String, T> fn) {
        var future = new CompletableFuture<T>();
        Runnable task = () -> {
            try {
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());
                debugLog.logResponse(response);
                if (response.statusCode() != okCode) {
                    future.completeExceptionally(new RuntimeException("Expected a status code of " + okCode
                            + " but it was " +  response.statusCode() + " from " + request.method() + " " + request.uri()));
                    return;
                }
                if (fn != null) {
                    var body = fn.apply(response.body());
                    future.complete(body);
                }
                else {
                    future.complete(null);
                }
            }
            catch (Exception e) {
                future.completeExceptionally(e);
            }
        };
        executor.execute(task);
        return future;
    }
    /**
     * Handle no content responses
     * @param request
     * @param okCode
     * @return
     */
    public CompletableFuture<Void> submit(HttpRequest request, int okCode) {
        return submit(request, okCode, null);
    }

    public <T> CompletableFuture<T> submitSearch(HttpRequest request,
                                                  int okCode,
                                                  Function<String, T> fn,
                                                  T defaultValue) {
        var future = new CompletableFuture<T>();
        Runnable task = () -> {
            try {
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());
                debugLog.logResponse(response);
                if (response.statusCode() == 404) {
                    log.atInfo().log(() -> "Not found for " + request.method() + " " + request.uri());
                    future.complete(defaultValue);
                }
                else if (response.statusCode() == okCode) {
                    var body = fn.apply(response.body());
                    future.complete(body);
                }
                else {
                    future.completeExceptionally(new RuntimeException("Expected a status code of " + okCode
                            + " but it was " +  response.statusCode() + " from " + request.method() + " " + request.uri()));
                }
            }
            catch (Exception e) {
                future.completeExceptionally(e);
            }
        };
        executor.execute(task);
        return future;
    }
}
