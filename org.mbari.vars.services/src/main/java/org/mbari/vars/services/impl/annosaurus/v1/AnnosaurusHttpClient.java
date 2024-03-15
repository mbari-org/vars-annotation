package org.mbari.vars.services.impl.annosaurus.v1;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.mbari.vars.core.util.InstantUtils;
import org.mbari.vars.core.util.MapUtils;
import org.mbari.vars.services.AnnotationService;
import org.mbari.vars.services.impl.BaseHttpClient;
import org.mbari.vars.services.impl.JwtHttpClient;
import org.mbari.vars.services.model.*;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;


public class AnnosaurusHttpClient extends BaseHttpClient implements AnnotationService {


    private final JwtHttpClient jwtHttpClient;

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

    public AnnosaurusHttpClient(HttpClient client, URI baseUri, String apiKey) {
        super(client, baseUri);
        this.jwtHttpClient = new JwtHttpClient(client,
                buildUri("/auth"),
                "Authorization", "APIKEY " + apiKey,
                body -> gson.fromJson(body, Authorization.class));
    }

    public AnnosaurusHttpClient(String baseUri, Duration timeout, String apikey) {
        this(newHttpClient(timeout), URI.create(baseUri), apikey);
    }

    private Authorization authorizeIfNeeded() {
        return jwtHttpClient.authorizeIfNeeded();
    }


    // --- API methods

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
        debugLog.logRequest(request, json);
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
        debugLog.logRequest(request, null);
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
        debugLog.logRequest(request, null);
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
        debugLog.logRequest(request, json);
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
        debugLog.logRequest(request, json);
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
        debugLog.logRequest(request, null);
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
        debugLog.logRequest(request, null);
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
        debugLog.logRequest(request, null);
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
        debugLog.logRequest(request, json);
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
        debugLog.logRequest(request, json);
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
        debugLog.logRequest(request, json);
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
        debugLog.logRequest(request, json);
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
        debugLog.logRequest(request, json);
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
        debugLog.logRequest(request, json);
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
        debugLog.logRequest(request, null);
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
        debugLog.logRequest(request, null);
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
        debugLog.logRequest(request, json);
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
        debugLog.logRequest(request, null);
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
        debugLog.logRequest(request, json);
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
        debugLog.logRequest(request, null);
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
        debugLog.logRequest(request, null);
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
        debugLog.logRequest(request, null);
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
        debugLog.logRequest(request, null);
        return submit(request, 200, body -> gson.fromJson(body, TYPE_LIST_STRING));
    }

    @Override
    public CompletableFuture<List<UUID>> findAllVideoReferenceUuids() {
        var uri = buildUri("/imagedmoments/videoreference");
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        debugLog.logRequest(request, null);
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
        debugLog.logRequest(request, null);
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
        debugLog.logRequest(request, null);
        return submitSearch(request, 200, body -> gson.fromJson(body, TYPE_LIST_ANCILLARY_DATA), new ArrayList<>());
    }

    @Override
    public CompletableFuture<List<Annotation>> findByConcept(String concept, Boolean data) {
        var query = MapUtils.mapToQueryFragment(Map.of("data", data));
        var uri = buildUri("/fast/concept/" + concept + query);
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        debugLog.logRequest(request, null);
        return submitSearch(request, 200, body -> gson.fromJson(body, TYPE_LIST_ANNOTATION), new ArrayList<>());
    }

    @Override
    public CompletableFuture<List<Annotation>> findByConcept(String concept, Long limit, Long offset, Boolean data) {
        var queryMap = MapUtils.of("limit", limit, "offset", offset, "data", data);
        var query = MapUtils.mapToQueryFragment(queryMap);
        var uri = buildUri("/fast/concept/" + concept + query);
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        debugLog.logRequest(request, null);
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

        var queryMap = MapUtils.of("limit", limit, "offset", offset, "data", data); // Failes with null values
        var query = MapUtils.mapToQueryFragment(queryMap);
        var uri = buildUri("/fast/videoreference/" + videoReferenceUuid + query);
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        debugLog.logRequest(request, null);
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
        debugLog.logRequest(request, null);
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
        debugLog.logRequest(httpRequest, json);
        return submit(httpRequest, 200, body -> gson.fromJson(body, ConceptAssociationResponse.class));
    }

    @Override
    public CompletableFuture<List<Annotation>> findByConcurrentRequest(ConcurrentRequest concurrentRequest, long limit, long offset) {
        var queryMap = MapUtils.of("limit", limit, "offset", offset);
        var query = MapUtils.mapToQueryFragment(queryMap);
        var uri = buildUri("/fast/concurrent" + query);
        var json = gson.toJson(concurrentRequest);
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        debugLog.logRequest(request, json);
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
        debugLog.logRequest(request, null);
        return submitSearch(request, 200, body -> gson.fromJson(body, TYPE_LIST_ANNOTATION), new ArrayList<>());
    }

    @Override
    public CompletableFuture<List<Annotation>> findByMultiRequest(MultiRequest multiRequest, long limit, long offset) {
        var queryMap = MapUtils.of("limit", limit, "offset", offset);
        var query = MapUtils.mapToQueryFragment(queryMap);
        var uri = buildUri("/fast/multi" + query);
        var json = gson.toJson(multiRequest);
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        debugLog.logRequest(request, json);
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
        debugLog.logRequest(request, null);
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
        debugLog.logRequest(request, null);
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
        debugLog.logRequest(request, null);
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
        debugLog.logRequest(request, null);
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
        debugLog.logRequest(request, null);
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
        debugLog.logRequest(request, null);
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
        debugLog.logRequest(request, null);
        return submitSearch(request, 200, body -> gson.fromJson(body, TYPE_LIST_IMAGED_MOMENT), new ArrayList<>());
    }

    @Override
    public CompletableFuture<List<Index>> findIndicesByVideoReferenceUuid(UUID videoReferenceUuid) {
        var uri = buildUri("/index/videoreference/" + videoReferenceUuid);
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        debugLog.logRequest(request, null);
        return submitSearch(request, 200, body -> gson.fromJson(body, TYPE_LIST_INDEX), new ArrayList<>());
    }

    @Override
    public CompletableFuture<CachedVideoReference> findVideoReferenceByVideoReferenceUuid(UUID videoReferenceUuid) {
        var uri = buildUri("/videoreferences/videoreference/" + videoReferenceUuid);
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        debugLog.logRequest(request, null);
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
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        debugLog.logRequest(request, json);
        return submit(request, 200, body -> gson.fromJson(body, TYPE_LIST_ANCILLARY_DATA));
    }

    @Override
    public CompletableFuture<ConceptsRenamed> renameConcepts(String oldConcept, String newConcept) {
        var uri = buildUri("/observations/concept/rename");
        var json = gson.toJson(Map.of("old", oldConcept, "new", newConcept));
        var auth = authorizeIfNeeded();
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "BEARER " + auth.getAccessToken())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        debugLog.logRequest(request, json);
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
        debugLog.logRequest(request, json);
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
        debugLog.logRequest(request, json);
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
        debugLog.logRequest(request, json);
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
        debugLog.logRequest(request, json);
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
        debugLog.logRequest(request, json);
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
        debugLog.logRequest(request, json);
        return submit(request, 200, body -> gson.fromJson(body, TYPE_LIST_INDEX));
    }

    @Override
    public CompletableFuture<Count> updateObservations(ObservationsUpdate update) {
        var uri = buildUri("/observations/bulk");
        var json = gson.toJson(update);
        var auth = authorizeIfNeeded();
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "BEARER " + auth.getAccessToken())
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        debugLog.logRequest(request, json);
        return submit(request, 200, body -> gson.fromJson(body, Count.class));
    }

    @Override
    public CompletableFuture<Collection<Annotation>> updateRecordedTimestampsForTapes(Collection<Annotation> annotations) {
        throw new UnsupportedOperationException("Use updateIndexRecordedTimestamps instead");
    }

    @Override
    public CompletableFuture<Optional<Index>> updateRecordedTimestamp(UUID imagedMomentUuid, Instant recordedTimestamp) {
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
        debugLog.logRequest(request, json);
        return submit(request, 200, body -> gson.fromJson(body, Index.class)).thenApply(Optional::ofNullable);
    }

    @Override
    public CompletableFuture<CachedVideoReference> updateCachedVideoReference(CachedVideoReference cvr) {
        var uri = buildUri("/videoreferences/" + cvr.getUuid());
        var json = gson.toJson(cvr);
        var auth = authorizeIfNeeded();
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "BEARER " + auth.getAccessToken())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        debugLog.logRequest(request, json);
        return submit(request, 200, body -> gson.fromJson(body, CachedVideoReference.class));
    }
}
