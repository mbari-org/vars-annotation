package org.mbari.vars.services.impl.vampiresquid.v1;

import org.mbari.vars.services.MediaService;
import org.mbari.vars.services.model.LastUpdate;
import org.mbari.vars.services.model.Media;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class VampireSquidHttpClient implements MediaService {



    @Override
    public CompletableFuture<Media> create(Media media) {
        return null;
    }

    @Override
    public CompletableFuture<Media> create(String videoSequenceName, String cameraId, String videoName, URI uri, Instant startTimestamp) {
        return null;
    }

    @Override
    public CompletableFuture<Media> update(UUID videoReferenceUuid, Instant startTimestamp, Duration duration) {
        return null;
    }

    @Override
    public CompletableFuture<Media> update(Media media) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> delete(UUID videoReferenceUuid) {
        return null;
    }

    @Override
    public CompletableFuture<Media> findByUuid(UUID uuid) {
        return null;
    }

    @Override
    public CompletableFuture<Media> findBySha512(byte[] sha512) {
        return null;
    }

    @Override
    public CompletableFuture<Media> findByUri(URI uri) {
        return null;
    }

    @Override
    public CompletableFuture<List<Media>> findByVideoSequenceName(String videoSequenceName) {
        return null;
    }

    @Override
    public CompletableFuture<List<Media>> findByVideoName(String videoName) {
        return null;
    }

    @Override
    public CompletableFuture<List<String>> findAllVideoSequenceNames() {
        return null;
    }

    @Override
    public CompletableFuture<List<Media>> findByCameraIdAndTimestamp(String cameraId, Instant timestamp) {
        return null;
    }

    @Override
    public CompletableFuture<List<Media>> findByCameraIdAndDate(String cameraId, Instant startTimestamp, Instant endTimestamp) {
        return null;
    }

    @Override
    public CompletableFuture<List<Media>> findByVideoSequenceNameAndTimestamp(String videoSequenceName, Instant timestamp) {
        return null;
    }

    @Override
    public CompletableFuture<List<String>> findAllCameraIds() {
        return null;
    }

    @Override
    public CompletableFuture<List<URI>> findAllURIs() {
        return null;
    }

    @Override
    public CompletableFuture<List<Media>> findConcurrentByVideoReferenceUuid(UUID uuid) {
        return null;
    }

    @Override
    public CompletableFuture<List<Media>> findByFilename(String filename) {
        return null;
    }

    @Override
    public CompletableFuture<LastUpdate> findLastVideoSequenceUpdate(UUID uuid) {
        return null;
    }

    @Override
    public CompletableFuture<LastUpdate> findLastVideoUpdate(UUID uuid) {
        return null;
    }

    @Override
    public CompletableFuture<LastUpdate> findLastVideoReferenceUpdate(UUID uuid) {
        return null;
    }

    @Override
    public CompletableFuture<List<String>> findVideoSequenceNamesByCameraId(String cameraId) {
        return null;
    }

    @Override
    public CompletableFuture<List<String>> findVideoNamesByVideoSequenceName(String videoSequenceName) {
        return null;
    }
}
