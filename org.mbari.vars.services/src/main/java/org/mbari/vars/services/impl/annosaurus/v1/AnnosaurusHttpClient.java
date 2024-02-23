package org.mbari.vars.services.impl.annosaurus.v1;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.mizosoft.methanol.Methanol;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.mbari.vars.core.util.InstantUtils;
import org.mbari.vars.core.util.Logging;
import org.mbari.vars.services.AnnotationService;
import org.mbari.vars.services.etc.methanol.LoggingInterceptor;
import org.mbari.vars.services.model.*;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AnnosaurusHttpClient implements AnnotationService {

    private final Logging log = new Logging(getClass());
    private final HttpClient client;
    private final URI baseUri;
    private final String apikey;
    private AtomicReference<Authorization> authorization = new AtomicReference<>();
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final Gson gson = AnnoWebServiceFactory.newGson();
    private final Type TYPE_LIST_ANCILLARY_DATA = new TypeToken<ArrayList<AncillaryData>>(){}.getType();
    private final Type TYPE_LIST_ANNOTATION = new TypeToken<ArrayList<Annotation>>(){}.getType();
    private final Type TYPE_LIST_ANNOTATION_COUNT = new TypeToken<ArrayList<AnnotationCount>>(){}.getType();
    private final Type TYPE_LIST_ASSOCIATION = new TypeToken<ArrayList<Association>>(){}.getType();
    private final Type TYPE_LIST_IMAGE = new TypeToken<ArrayList<Image>>(){}.getType();
    private final Type TYPE_LIST_IMAGED_MOMENT = new TypeToken<ArrayList<ImagedMoment>>(){}.getType();
    private final Type TYPE_LIST_INDEX = new TypeToken<ArrayList<Index>>(){}.getType();
    private final Type TYPE_LIST_STRING = new TypeToken<ArrayList<String>>(){}.getType();
    private final Type TYPE_LIST_USER = new TypeToken<ArrayList<User>>(){}.getType();
    private final Type TYPE_LIST_UUID = new TypeToken<ArrayList<UUID>>(){}.getType();

    public AnnosaurusHttpClient(String baseUri, Duration timeout, String apikey) {
        this.baseUri = URI.create(baseUri);
        this.apikey = apikey;
        client = Methanol.newBuilder()
                .autoAcceptEncoding(true)
                .connectTimeout(timeout)
                .executor(executor)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .readTimeout(timeout)
                .requestTimeout(timeout)
                .userAgent("org.mbari.vars.services")
                .build();
    }

    private URI buildUri(String path) {
        var newPath = baseUri.getPath() + path;
        return baseUri.resolve(newPath);

    }

    private String mapToQueryFragment(Map<String, ?> map) {
        return map.entrySet()
                .stream()
                .filter(e -> e.getValue() != null)
                .map(e -> e.getKey() + "=" + e.getValue())
                .reduce((a, b) -> a + "&" + b)
                .map(s -> "?" + s)
                .orElse("");
    }

    private void logResponse(HttpResponse<?> response) {
        if (log.logger().isLoggable(System.Logger.Level.DEBUG)) {
            var req = response.request();
            var headers = response.headers().map().entrySet().stream()
                    .map(e -> e.getKey() + ": " + String.join(", ", e.getValue()))
                    .collect(Collectors.joining(System.lineSeparator()));
            log.atDebug().log(() -> "RECEIVED: " + req.method() + " " + req.uri() + " [" + response.statusCode()
                    + "] \n" + headers + "\n\n" + response.body());
        }
    }

    private void logRequest(HttpRequest request, String body) {
        if (log.logger().isLoggable(System.Logger.Level.DEBUG)) {
            var headers = request.headers().map().entrySet().stream()
                    .map(e -> e.getKey() + ": " + String.join(", ", e.getValue()))
                    .collect(Collectors.joining(System.lineSeparator()));
            var bodyString = (body == null) ? "" : "\n\n" + body;
            log.atDebug().log(() -> "SENDING: " + request.method() + " " + request.uri() + "\n" + headers + bodyString);
        }
    }


    /**
     * Handle a request
     * @param request The resuest to send
     * @param okCode The expected code if completed successfully
     * @param fn A function to handle the response body. If null then the body is ignored
     * @return A CompletableFuture that will complete when the request is done
     * @param <T> The type that the response body will be converted to
     */
    private <T> CompletableFuture<T> submit(HttpRequest request,
                                            int okCode,
                                            Function<String, T> fn) {
        var future = new CompletableFuture<T>();
        Runnable task = () -> {
            try {
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());
                logResponse(response);
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

    private <T> CompletableFuture<T> submitSearch(HttpRequest request,
                                                  int okCode,
                                                  Function<String, T> fn,
                                                  T defaultValue) {
        var future = new CompletableFuture<T>();
        Runnable task = () -> {
            try {
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());
                logResponse(response);
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


    /**
     * Handle no content responses
     * @param request
     * @param okCode
     * @return
     */
    private CompletableFuture<Void> submit(HttpRequest request, int okCode) {
        return submit(request, okCode, null);
    }

    // ---- Authorization stuff
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

    private Authorization authorizeIfNeeded() {
        return authorization.updateAndGet(this::reauthorize);
    }

    private Authorization reauthorize(Authorization a) {
        if ((a == null) || isExpired(a)) {
            return authorize(apikey).join();
        }
        return a;
    }

    public CompletableFuture<Authorization> authorize(String apikey) {
        var uri = buildUri("/auth");
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "APIKEY " + apikey)
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        logRequest(request, null);
        return submit(request, 200, body -> gson.fromJson(body, Authorization.class));
    }


    public CompletableFuture<Annotation> createAnnotation(Annotation annotation) {
        var json = gson.toJson(annotation);
        var uri = buildUri("/annotations");
        var auth = authorizeIfNeeded();
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "BEARER " + auth.getAccessToken())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        logRequest(request, json);
        return submit(request, 200, body -> gson.fromJson(body, Annotation.class));
    }

    @Override
    public CompletableFuture<AnnotationCount> countAnnotations(UUID videoReferenceUuid) {
        var uri = buildUri("/observations/videoreference/count/" + videoReferenceUuid);
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        logRequest(request, null);
        return submitSearch(request, 200,
                body -> gson.fromJson(body, AnnotationCount.class),
                new AnnotationCount(videoReferenceUuid, 0));
    }

    @Override
    public CompletableFuture<List<AnnotationCount>> countAnnotationsGroupByVideoReferenceUuid() {
        var uri = buildUri("/observations/counts");
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        logRequest(request, null);
        return submit(request, 200, body -> gson.fromJson(body, TYPE_LIST_ANNOTATION_COUNT));
    }

    @Override
    public CompletableFuture<ConcurrentRequestCount> countByConcurrentRequest(ConcurrentRequest concurrentRequest) {
        var uri = buildUri("/annotations/concurrent/count");
        var json = gson.toJson(concurrentRequest);
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        logRequest(request, json);
        return submit(request, 200, body -> gson.fromJson(body, ConcurrentRequestCount.class));
    }

    @Override
    public CompletableFuture<MultiRequestCount> countByMultiRequest(MultiRequest multiRequest) {
        var uri = buildUri("/annotations/multi/count");
        var json = gson.toJson(multiRequest);
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        logRequest(request, json);
        return submit(request, 200, body -> gson.fromJson(body, MultiRequestCount.class));
    }

    @Override
    public CompletableFuture<List<AnnotationCount>> countImagedMomentsGroupByVideoReferenceUuid() {
        var uri = buildUri("/imagedmoments/counts");
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        logRequest(request, null);
        return submit(request, 200, body -> gson.fromJson(body, TYPE_LIST_ANNOTATION_COUNT));
    }

    @Override
    public CompletableFuture<ConceptCount> countObservationsByConcept(String concept) {
        var uri = buildUri("/observations/concept/count/" + concept);
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        logRequest(request, null);
        return submitSearch(request, 200,
                body -> gson.fromJson(body, ConceptCount.class),
                new ConceptCount(concept, 0));
    }

    @Override
    public CompletableFuture<AnnotationCount> countImagedMomentsModifiedBefore(UUID videoReferenceUuid, Instant date) {
        var t = InstantUtils.COMPACT_TIME_FORMATTER_MS.format(date);
        var uri = buildUri("/imagedmoments/videoreference/modified/" + videoReferenceUuid + "/" + t);
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        logRequest(request, null);
        return submitSearch(request, 200,
                body -> gson.fromJson(body, AnnotationCount.class),
                new AnnotationCount(videoReferenceUuid, 0));
    }

    @Override
    public CompletableFuture<Collection<Annotation>> createAnnotations(Collection<Annotation> annotations) {
        var uri = buildUri("/annotations/bulk");
        var json = gson.toJson(annotations);
        var auth = authorizeIfNeeded();
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "BEARER " + auth.getAccessToken())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        logRequest(request, json);
        return submit(request, 200, body -> gson.fromJson(body, TYPE_LIST_ANNOTATION));
    }

    @Override
    public CompletableFuture<Association> createAssociation(UUID observationUuid, Association association) {
        var uri = buildUri("/associations");
        var ac = new AssociationCreate(observationUuid, association);
        var json = gson.toJson(ac);
        var auth = authorizeIfNeeded();
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "BEARER " + auth.getAccessToken())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        logRequest(request, json);
        return submit(request, 200, body -> gson.fromJson(body, Association.class));
    }

    @Override
    public CompletableFuture<Association> createAssociation(UUID observationUuid, Association association, UUID associationUuid) {
        var uri = buildUri("/associations");
        var dto = new AssociationCreate(observationUuid, association);
        var json = gson.toJson(dto);
        var auth = authorizeIfNeeded();
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "BEARER " + auth.getAccessToken())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        logRequest(request, json);
        return submit(request, 200, body -> gson.fromJson(body, Association.class));
    }

    @Override
    public CompletableFuture<Image> createImage(Image image) {
        var uri = buildUri("/images");
        var json = gson.toJson(image);
        var auth = authorizeIfNeeded();
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "BEARER " + auth.getAccessToken())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        logRequest(request, json);
        return submit(request, 200, body -> gson.fromJson(body, Image.class));
    }

    @Override
    public CompletableFuture<List<AncillaryData>> createOrUpdateAncillaryData(List<AncillaryData> ancillaryData) {
        var uri = buildUri("/ancillarydata/bulk");
        var json = gson.toJson(ancillaryData);
        var auth = authorizeIfNeeded();
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "BEARER " + auth.getAccessToken())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        logRequest(request, json);
        return submit(request, 200, body -> gson.fromJson(body, TYPE_LIST_ANCILLARY_DATA));
    }

    @Override
    public CompletableFuture<CachedVideoReference> createCachedVideoReference(CachedVideoReference cvr) {
        var uri = buildUri("/videoreferences");
        var json = gson.toJson(cvr);
        var auth = authorizeIfNeeded();
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "BEARER " + auth.getAccessToken())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        logRequest(request, json);
        return submit(request, 200, body -> gson.fromJson(body, CachedVideoReference.class));
    }

    @Override
    public CompletableFuture<AncillaryDataDeleteCount> deleteAncillaryDataByVideoReference(UUID videoReferenceUuid) {
        var uri = buildUri("/ancillarydata/videoreference/" + videoReferenceUuid);
        var auth = authorizeIfNeeded();
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "BEARER " + auth.getAccessToken())
                .header("Accept", "application/json")
                .DELETE()
                .build();
        logRequest(request, null);
        return submit(request, 200, body -> gson.fromJson(body, AncillaryDataDeleteCount.class));
    }

    @Override
    public CompletableFuture<Boolean> deleteAnnotation(UUID observationUuid) {
        var uri = buildUri("/observations/" + observationUuid);
        var auth = authorizeIfNeeded();
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "BEARER " + auth.getAccessToken())
                .DELETE()
                .build();
        logRequest(request, null);
        return submit(request, 204).thenApply(v -> true);
    }

    @Override
    public CompletableFuture<Boolean> deleteAnnotations(Collection<UUID> observationUuids) {
        var uri = buildUri("/observations/delete");
        var json = gson.toJson(observationUuids);
        var auth = authorizeIfNeeded();
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "BEARER " + auth.getAccessToken())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        logRequest(request, json);
        return submit(request, 204).thenApply(v -> true);
    }

    @Override
    public CompletableFuture<Boolean> deleteAssociation(UUID associationUuid) {
        var uri = buildUri("/associations/" + associationUuid);
        var auth = authorizeIfNeeded();
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "BEARER " + auth.getAccessToken())
                .DELETE()
                .build();
        logRequest(request, null);
        return submit(request, 204).thenApply(v -> true);
    }

    @Override
    public CompletableFuture<Boolean> deleteAssociations(Collection<UUID> associationUuids) {
        var uri = buildUri("/associations/delete");
        var json = gson.toJson(associationUuids);
        var auth = authorizeIfNeeded();
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "BEARER " + auth.getAccessToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        logRequest(request, json);
        return submit(request, 204).thenApply(v -> true);
    }

    @Override
    public CompletableFuture<Boolean> deleteImage(UUID imageReferenceUuid) {
        var uri = buildUri("/imagereferences/" + imageReferenceUuid);
        var auth = authorizeIfNeeded();
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "BEARER " + auth.getAccessToken())
                .DELETE()
                .build();
        logRequest(request, null);
        return submit(request, 204).thenApply(v -> true);
    }

    @Override
    public CompletableFuture<Annotation> deleteDuration(UUID observationUuid) {
        var uri = buildUri("/observations/delete/duration/" + observationUuid);
        var auth = authorizeIfNeeded();
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "BEARER " + auth.getAccessToken())
                .header("Accept", "application/json")
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        logRequest(request, null);
        return submit(request, 200, body -> gson.fromJson(body, Annotation.class));
    }

    @Override
    public CompletableFuture<Boolean> deleteCacheVideoReference(UUID uuid) {
        var uri = buildUri("/videoreferences/" + uuid);
        var auth = authorizeIfNeeded();
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "BEARER " + auth.getAccessToken())
                .DELETE()
                .build();
        logRequest(request, null);
        return submit(request, 204).thenApply(v -> true);
    }

    @Override
    public CompletableFuture<List<String>> findActivities() {
        var uri = buildUri("/observations/activities");
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        logRequest(request, null);
        return submit(request, 200, body -> gson.fromJson(body, TYPE_LIST_STRING));
    }

    @Override
    public CompletableFuture<List<UUID>> findAllVideoReferenceUuids() {
        var uri = buildUri("/videoreferences/videoreferences");
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        logRequest(request, null);
        return submit(request, 200, body -> gson.fromJson(body, TYPE_LIST_UUID));
    }

    @Override
    public CompletableFuture<AncillaryData> findAncillaryData(UUID observationUuid) {
        var uri = buildUri("/ancillarydata/observation/" + observationUuid);
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        logRequest(request, null);
        return submitSearch(request, 200, body -> gson.fromJson(body, AncillaryData.class), null);
    }

    @Override
    public CompletableFuture<List<AncillaryData>> findAncillaryDataByVideoReference(UUID videoReferenceUuid) {
        var uri = buildUri("/ancillarydata/videoreference/" + videoReferenceUuid);
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        logRequest(request, null);
        return submitSearch(request, 200, body -> gson.fromJson(body, TYPE_LIST_ANCILLARY_DATA), new ArrayList<>());
    }

    @Override
    public CompletableFuture<List<Annotation>> findByConcept(String concept, Boolean data) {
        var query = mapToQueryFragment(Map.of("data", data));
        var uri = buildUri("/fast/concept/" + concept + query);
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        logRequest(request, null);
        return submitSearch(request, 200, body -> gson.fromJson(body, TYPE_LIST_ANNOTATION), new ArrayList<>());
    }

    @Override
    public CompletableFuture<List<Annotation>> findByConcept(String concept, Long limit, Long offset, Boolean data) {
        var queryMap = Map.of("limit", limit, "offset", offset, "data", data);
        var query = mapToQueryFragment(queryMap);
        var uri = buildUri("/fast/concept/" + concept + query);
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        logRequest(request, null);
        return submitSearch(request, 200, body -> gson.fromJson(body, TYPE_LIST_ANNOTATION), new ArrayList<>());
    }

    @Override
    public CompletableFuture<List<Annotation>> findAnnotations(UUID videoReferenceUuid) {
        return findAnnotations(videoReferenceUuid, null, null, false);
    }

    @Override
    public CompletableFuture<List<Annotation>> findAnnotations(UUID videoReferenceUuid, boolean data) {
        return findAnnotations(videoReferenceUuid, null, null, data);
    }

    @Override
    public CompletableFuture<List<Annotation>> findAnnotations(UUID videoReferenceUuid, Long limit, Long offset) {
        return findAnnotations(videoReferenceUuid, limit, offset, false);
    }

    @Override
    public CompletableFuture<List<Annotation>> findAnnotations(UUID videoReferenceUuid, Long limit, Long offset, Boolean data) {
        var queryMap = Map.of("limit", limit, "offset", offset, "data", data);
        var query = mapToQueryFragment(queryMap);
        var uri = buildUri("/fast/videoreference/" + videoReferenceUuid + query);
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        logRequest(request, null);
        return submitSearch(request, 200, body -> gson.fromJson(body, TYPE_LIST_ANNOTATION), new ArrayList<>());
    }

    @Override
    public CompletableFuture<Association> findAssociationByUuid(UUID associationUuid) {
        var uri = buildUri("/associations/" + associationUuid);
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        logRequest(request, null);
        return submitSearch(request, 200, body -> gson.fromJson(body, Association.class), null);
    }

    @Override
    public CompletableFuture<ConceptAssociationResponse> findByConceptAssociationRequest(ConceptAssociationRequest request) {
        var uri = buildUri("/associations/conceptassociations");
        var json = gson.toJson(request);
        var httpRequest = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        logRequest(httpRequest, json);
        return submit(httpRequest, 200, body -> gson.fromJson(body, ConceptAssociationResponse.class));
    }

    @Override
    public CompletableFuture<List<Annotation>> findByConcurrentRequest(ConcurrentRequest concurrentRequest, long limit, long offset) {
        var queryMap = Map.of("limit", limit, "offset", offset);
        var query = mapToQueryFragment(queryMap);
        var uri = buildUri("/fast/concurrent" + query);
        var json = gson.toJson(concurrentRequest);
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        logRequest(request, json);
        return submit(request, 200, body -> gson.fromJson(body, TYPE_LIST_ANNOTATION));
    }

    @Override
    public CompletableFuture<List<Annotation>> findByImageReference(UUID imageReferenceUuid) {
        var uri = buildUri("/annotations/imagereference/" + imageReferenceUuid);
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        logRequest(request, null);
        return submitSearch(request, 200, body -> gson.fromJson(body, TYPE_LIST_ANNOTATION), new ArrayList<>());
    }

    @Override
    public CompletableFuture<List<Annotation>> findByMultiRequest(MultiRequest multiRequest, long limit, long offset) {
        var queryMap = Map.of("limit", limit, "offset", offset);
        var query = mapToQueryFragment(queryMap);
        var uri = buildUri("/fast/multi" + query);
        var json = gson.toJson(multiRequest);
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        logRequest(request, json);
        return submitSearch(request, 200, body -> gson.fromJson(body, TYPE_LIST_ANNOTATION), new ArrayList<>());
    }

    @Override
    public CompletableFuture<Annotation> findByUuid(UUID observationUuid) {
        var uri = buildUri("/annotations/" + observationUuid);
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        logRequest(request, null);
        return submitSearch(request, 200, body -> gson.fromJson(body, Annotation.class), null);
    }

    @Override
    public CompletableFuture<List<Association>> findByVideoReferenceAndLinkName(UUID videoReferenceUuid, String linkName) {
        var uri = buildUri("/associations/" + videoReferenceUuid + "/" + linkName);
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        logRequest(request, null);
        return submitSearch(request, 200, body -> gson.fromJson(body, TYPE_LIST_ASSOCIATION), new ArrayList<>());
    }

    @Override
    public CompletableFuture<List<Association>> findByVideoReferenceAndLinkNameAndConcept(UUID videoReferenceUuid, String linkName, String concept) {
        var uri = buildUri("/associations/" + videoReferenceUuid + "/" + linkName + "?concept=" + concept);
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        logRequest(request, null);
        return submitSearch(request, 200, body -> gson.fromJson(body, TYPE_LIST_ASSOCIATION), new ArrayList<>());
    }

    @Override
    public CompletableFuture<List<String>> findGroups() {
        var uri = buildUri("/observations/groups");
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        logRequest(request, null);
        return submit(request, 200, body -> gson.fromJson(body, TYPE_LIST_STRING));
    }

    @Override
    public CompletableFuture<Image> findImageByUrl(URL url) {
        var encodedUrl = URLEncoder.encode(url.toString(), StandardCharsets.UTF_8);
        var uri = buildUri("/images/url/" + encodedUrl);
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        logRequest(request, null);
        return submitSearch(request, 200, body -> gson.fromJson(body, Image.class), null);
    }

    @Override
    public CompletableFuture<Image> findImageByUuid(UUID imageReferenceUuid) {
        var uri = buildUri("/images/" + imageReferenceUuid);
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        return submitSearch(request, 200, body -> gson.fromJson(body, Image.class), null);
    }

    @Override
    public CompletableFuture<List<Image>> findImagesByVideoReferenceUuid(UUID videoReferenceUuid) {
        var uri = buildUri("/fast/images/videoreference/" + videoReferenceUuid);
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        logRequest(request, null);
        return submitSearch(request, 200, body -> gson.fromJson(body, TYPE_LIST_IMAGE), new ArrayList<>());
    }

    @Override
    public CompletableFuture<List<ImagedMoment>> findImagedMomentsByVideoReferenceUuid(UUID videoReferenceUuid) {
        var uri = buildUri("/imagedmoments/videoreference/" + videoReferenceUuid);
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        logRequest(request, null);
        return submitSearch(request, 200, body -> gson.fromJson(body, TYPE_LIST_IMAGED_MOMENT), new ArrayList<>());
    }

    @Override
    public CompletableFuture<List<Index>> findIndicesByVideoReferenceUuid(UUID videoReferenceUuid) {
        var uri = buildUri("/indices/videoreference/" + videoReferenceUuid);
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        logRequest(request, null);
        return submitSearch(request, 200, body -> gson.fromJson(body, TYPE_LIST_INDEX), new ArrayList<>());
    }

    @Override
    public CompletableFuture<CachedVideoReference> findVideoReferenceByVideoReferenceUuid(UUID videoReferenceUuid) {
        var uri = buildUri("/videoreferences/" + videoReferenceUuid);
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        logRequest(request, null);
        return submitSearch(request, 200, body -> gson.fromJson(body, CachedVideoReference.class), null);
    }

    @Override
    public CompletableFuture<Collection<AncillaryData>> merge(UUID videoReferenceUuid, Collection<AncillaryData> data) {
        var uri = buildUri("/ancillarydata/merge/" + videoReferenceUuid);
        var json = gson.toJson(data);
        var auth = authorizeIfNeeded();
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "BEARER " + auth.getAccessToken())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        logRequest(request, json);
        return submit(request, 200, body -> gson.fromJson(body, TYPE_LIST_ANCILLARY_DATA));
    }

    @Override
    public CompletableFuture<ConceptsRenamed> renameConcepts(String oldConcept, String newConcept) {
        var uri = buildUri("/observations/concept/rename");
        var json = gson.toJson(Map.of("old", oldConcept, "new", newConcept));
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        logRequest(request, json);
        return submit(request, 200, body -> gson.fromJson(body, ConceptsRenamed.class));
    }

    @Override
    public CompletableFuture<Annotation> updateAnnotation(Annotation annotation) {
        var uri = buildUri("/annotations/" + annotation.getObservationUuid());
        var json = gson.toJson(annotation);
        var auth = authorizeIfNeeded();
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "BEARER " + auth.getAccessToken())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        logRequest(request, json);
        return submit(request, 200, body -> gson.fromJson(body, Annotation.class));
    }

    @Override
    public CompletableFuture<Collection<Annotation>> updateAnnotations(Collection<Annotation> annotations) {
        var uri = buildUri("/annotations/bulk");
        var json = gson.toJson(annotations);
        var auth = authorizeIfNeeded();
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "BEARER " + auth.getAccessToken())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        logRequest(request, json);
        return submit(request, 200, body -> gson.fromJson(body, TYPE_LIST_ANNOTATION));
    }

    @Override
    public CompletableFuture<Association> updateAssociation(Association association) {
        var uri = buildUri("/associations/" + association.getUuid());
        var json = gson.toJson(association);
        var auth = authorizeIfNeeded();
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "BEARER " + auth.getAccessToken())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        logRequest(request, json);
        return submit(request, 200, body -> gson.fromJson(body, Association.class));
    }

    @Override
    public CompletableFuture<Collection<Association>> updateAssociations(Collection<Association> associations) {
        var uri = buildUri("/associations/bulk");
        var json = gson.toJson(associations);
        var auth = authorizeIfNeeded();
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "BEARER " + auth.getAccessToken())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        logRequest(request, json);
        return submit(request, 200, body -> gson.fromJson(body, TYPE_LIST_ASSOCIATION));
    }

    @Override
    public CompletableFuture<Image> updateImage(Image image) {
        var uri = buildUri("/images/" + image.getImageReferenceUuid());
        var json = gson.toJson(image);
        var auth = authorizeIfNeeded();
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "BEARER " + auth.getAccessToken())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        logRequest(request, json);
        return submit(request, 200, body -> gson.fromJson(body, Image.class));
    }

    @Override
    public CompletableFuture<List<Index>> updateIndexRecordedTimestamps(Collection<Index> indices) {
        var uri = buildUri("/index/tapetime");
        var json = gson.toJson(indices);
        var auth = authorizeIfNeeded();
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "BEARER " + auth.getAccessToken())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        logRequest(request, json);
        return submit(request, 200, body -> gson.fromJson(body, TYPE_LIST_INDEX));
    }

    @Override
    public CompletableFuture<Collection<Annotation>> updateRecordedTimestampsForTapes(Collection<Annotation> annotations) {
        throw new UnsupportedOperationException("Use updateIndexRecordedTimestamps instead");
    }

    @Override
    public CompletableFuture<Optional<ImagedMoment>> updateRecordedTimestamp(UUID imagedMomentUuid, Instant recordedTimestamp) {
        Map<String, String> map = Map.of("recorded_timestamp", recordedTimestamp.toString());
        var uri = buildUri("/imagedmoments/" + imagedMomentUuid);
        var json = gson.toJson(map);
        var auth = authorizeIfNeeded();
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "BEARER " + auth.getAccessToken())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        logRequest(request, json);
        return submit(request, 200, body -> gson.fromJson(body, ImagedMoment.class)).thenApply(Optional::ofNullable);
    }

    @Override
    public CompletableFuture<CachedVideoReference> updateCachedVideoReference(CachedVideoReference cvr) {
        var uri = buildUri("/videoreferences/" + cvr.getVideoReferenceUuid());
        var json = gson.toJson(cvr);
        var auth = authorizeIfNeeded();
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "BEARER " + auth.getAccessToken())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        logRequest(request, json);
        return submit(request, 200, body -> gson.fromJson(body, CachedVideoReference.class));
    }
}
