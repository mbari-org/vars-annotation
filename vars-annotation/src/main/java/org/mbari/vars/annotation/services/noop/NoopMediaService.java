package org.mbari.vars.annotation.services.noop;

import org.mbari.vars.vampiresquid.sdk.r1.MediaService;
import org.mbari.vars.vampiresquid.sdk.r1.models.LastUpdate;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class NoopMediaService implements MediaService {
    @Override
    public CompletableFuture<Media> create(Media media) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Media> create(String videoSequenceName, String cameraId, String videoName, URI uri, Instant startTimestamp) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Media> update(UUID videoReferenceUuid, Instant startTimestamp, Duration duration) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Media> update(Media media) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Boolean> delete(UUID videoReferenceUuid) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Media> findByUuid(UUID uuid) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Media> findBySha512(byte[] sha512) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Media> findByUri(URI uri) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<List<Media>> findByVideoSequenceName(String videoSequenceName) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<Media>> findByVideoName(String videoName) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<String>> findAllVideoSequenceNames() {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<Media>> findByCameraIdAndTimestamp(String cameraId, Instant timestamp) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<Media>> findByCameraIdAndDate(String cameraId, Instant startTimestamp, Instant endTimestamp) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<Media>> findByVideoSequenceNameAndTimestamp(String videoSequenceName, Instant timestamp) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<String>> findAllCameraIds() {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<URI>> findAllURIs() {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<Media>> findConcurrentByVideoReferenceUuid(UUID uuid) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<Media>> findByFilename(String filename) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<LastUpdate> findLastVideoSequenceUpdate(UUID uuid) {
        return CompletableFuture.completedFuture(new LastUpdate(Instant.EPOCH));
    }

    @Override
    public CompletableFuture<LastUpdate> findLastVideoUpdate(UUID uuid) {
        return CompletableFuture.completedFuture(new LastUpdate(Instant.EPOCH));
    }

    @Override
    public CompletableFuture<LastUpdate> findLastVideoReferenceUpdate(UUID uuid) {
        return CompletableFuture.completedFuture(new LastUpdate(Instant.EPOCH));
    }

    @Override
    public CompletableFuture<List<String>> findVideoSequenceNamesByCameraId(String cameraId) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<String>> findVideoNamesByVideoSequenceName(String videoSequenceName) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<Media>> listVideoSequences(int pageNumber, int pageSize) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }
}
