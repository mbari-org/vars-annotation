package org.mbari.vars.services.impl.annosaurus.v1;

import com.github.mizosoft.methanol.Methanol;
import com.google.gson.Gson;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Authorization;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class AnnosaurusHttpClient {
    private final HttpClient client;
    private final URI baseUri;
    private AtomicReference<Authorization> authorization = new AtomicReference<>();
    private final Gson gson = AnnoWebServiceFactory.newGson();

    public AnnosaurusHttpClient(String baseUri, Duration timeout) {
        this.baseUri = URI.create(baseUri);
        client = Methanol.newBuilder()
                .autoAcceptEncoding(true)
                .connectTimeout(timeout)
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .readTimeout(timeout)
                .requestTimeout(timeout)
                .userAgent("org.mbari.vars.services")
                .build();
    }

    public CompletableFuture<Optional<Authorization>> authorize(String apikey) {
//        var uri = baseUri.resolve("/auth");
//
//        var request = HttpRequest.newBuilder()
//                .uri(uri)
//                .header("Authorization", "APIKEY " + apikey)
//                .POST(HttpRequest.BodyPublishers.noBody())
//                .build();
//
//        var response =  client.send(request, HttpResponse.BodyHandlers.ofString());
//        switch (response.statusCode()) {
//            case 200 -> {
//                var auth = Authorization.parse(response.body());
//                authorization.set(auth);
//                return CompletableFuture.completedFuture(Optional.of(auth));
//            }
//            case 401 -> {
//                authorization.set(null);
//                return CompletableFuture.completedFuture(Optional.empty());
//            }
//        }
        return null;
    }



    public CompletableFuture<Annotation> createAnnotation(Annotation annotation) {
        Long durationMillis = (annotation.getDuration() == null) ? null : annotation.getDuration()
                .toMillis();
        Long elapsedTimeMilliis = (annotation.getElapsedTime() == null)
                ? null : annotation.getElapsedTime()
                .toMillis();

        return null;
    }
}
