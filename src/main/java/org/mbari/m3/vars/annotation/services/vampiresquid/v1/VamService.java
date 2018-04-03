package org.mbari.m3.vars.annotation.services.vampiresquid.v1;

import org.mbari.m3.vars.annotation.gson.ByteArrayConverter;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.services.AuthService;
import org.mbari.m3.vars.annotation.services.MediaService;
import org.mbari.m3.vars.annotation.services.RetrofitWebService;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author Brian Schlining
 * @since 2017-05-27T12:32:00
 */
public class VamService implements MediaService, RetrofitWebService {

    private final VamWebService vamWebService;
    private final Map<String, String> defaultHeaders;

    @Inject
    public VamService(VamWebServiceFactory serviceFactory, @Named("MEDIA_AUTH") AuthService authService) {
        vamWebService = serviceFactory.create(VamWebService.class, authService);
        defaultHeaders = new HashMap<>();
        defaultHeaders.put("Accept", "application/json");
        defaultHeaders.put("Accept-Charset", "utf-8");
    }

    @Override
    public CompletableFuture<Media> create(String videoSequenceName,
            String cameraId,
            String videoName,
            URI uri,
            Instant startTimestamp) {
        return sendRequest(vamWebService.create(videoSequenceName,
                cameraId,
                videoName,
                uri,
                startTimestamp,
                null, null, null, null,
                null, null, null, null,
                null, null, defaultHeaders));
    }

    public CompletableFuture<Media> update(UUID videoUuid,
            Instant startTimestamp,
            Duration duration) {

        Map<String, String> fieldMap = new HashMap<>();
        addField(fieldMap, "duration_millis", duration.toMillis());
        addField(fieldMap, "start", startTimestamp);
        return sendRequest(vamWebService.update(videoUuid,
                fieldMap, defaultHeaders));
    }

    @Override
    public CompletableFuture<Media> findByUuid(UUID uuid) {
        return sendRequest(vamWebService.findByUuid(uuid));
    }

    @Override
    public CompletableFuture<Media> findBySha512(byte[] sha512) {
        String hex = ByteArrayConverter.encode(sha512);
        return sendRequest(vamWebService.findBySha512(hex));
    }

    @Override
    public CompletableFuture<Media> findByUri(URI uri) {
        return sendRequest(vamWebService.findByUri(uri));
    }

    @Override
    public CompletableFuture<List<Media>> findByVideoSequenceName(String videoSequenceName) {
        return sendRequest(vamWebService.findByVideoSequenceName(videoSequenceName));
    }

    @Override
    public CompletableFuture<List<Media>> findByVideoName(String videoName) {
        return sendRequest(vamWebService.findByVideoName(videoName));
    }

    @Override
    public CompletableFuture<List<String>> findAllVideoSequenceNames() {
        return sendRequest(vamWebService.findAllVideoSequenceNames());
    }

    @Override
    public CompletableFuture<List<Media>> findByCameraIdAndTimestamp(String cameraId, Instant timestamp) {
        return sendRequest(vamWebService.findByCameraIdAndTimestamp(cameraId, timestamp));
    }

    @Override
    public CompletableFuture<List<Media>> findByVideoSequenceNameAndTimestamp(String videoSequenceName, Instant timestamp) {
        return CompletableFuture.completedFuture(new ArrayList<>());
    }

    @Override
    public CompletableFuture<List<Media>> findConcurrentByVideoReferenceUuid(UUID uuid) {
        return sendRequest(vamWebService.findConcurrent(uuid));
    }

    @Override
    public CompletableFuture<List<String>> findAllCameraIds() {
        return sendRequest(vamWebService.findAllCameraIds());
    }

    @Override
    public CompletableFuture<List<String>> findVideoSequenceNamesByCameraId(String cameraId) {
        return sendRequest(vamWebService.findVideoSequenceNamesByCameraId(cameraId));
    }

    @Override
    public CompletableFuture<List<String>> findVideoNamesByVideoSequenceName(String videoSequenceName) {
        return sendRequest(vamWebService.findVideoNamesByVideoSequenceName(videoSequenceName));
    }

    public CompletableFuture<List<Media>> findByFilename(String filename) {
        return sendRequest(vamWebService.findByFilename(filename));
    }

    private void addField(Map<String, String> map, String key, Object value) {
        if (value != null) {
            map.put(key, asString(value));
        }
    }
}

