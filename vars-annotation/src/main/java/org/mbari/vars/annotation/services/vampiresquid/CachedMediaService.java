package org.mbari.vars.annotation.services.vampiresquid;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.mbari.vars.vampiresquid.sdk.r1.MediaService;
import org.mbari.vars.vampiresquid.sdk.r1.models.LastUpdate;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class CachedMediaService implements MediaService {

    private final MediaService mediaService;
    private final AsyncLoadingCache<UUID, Optional<Media>> mediaCache;

    public CachedMediaService(MediaService mediaService) {
        this.mediaService = mediaService;
        mediaCache = Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .maximumSize(100)
                .buildAsync((key, executor) -> mediaService.findByUuid(key).thenApply(Optional::ofNullable));
    }

    public synchronized void clear() {
        mediaCache.synchronous().invalidateAll();
    }

    @Override
    public CompletableFuture<Media> create(Media media) {
        return cacheSingle(mediaService.create(media));
    }

    @Override
    public CompletableFuture<Media> create(String videoSequenceName, String cameraId, String videoName, URI uri, Instant startTimestamp) {
        return cacheSingle(mediaService.create(videoSequenceName, cameraId, videoName, uri, startTimestamp));
    }

    @Override
    public CompletableFuture<Media> update(UUID videoReferenceUuid, Instant startTimestamp, Duration duration) {
        return cacheSingle(mediaService.update(videoReferenceUuid, startTimestamp, duration));
    }

    @Override
    public CompletableFuture<Media> update(Media media) {
        return cacheSingle(mediaService.update(media));
    }

    @Override
    public CompletableFuture<Boolean> delete(UUID videoReferenceUuid) {
        return mediaService.delete(videoReferenceUuid)
                .thenApply(ok -> {
                    if (ok) {
                        mediaCache.synchronous().invalidate(videoReferenceUuid);
                    }
                    return ok;
                });
    }

    @Override
    public CompletableFuture<Media> findByUuid(UUID uuid) {
        return mediaCache.get(uuid).thenApply(m -> m.orElse(null));
    }


    @Override
    public CompletableFuture<Media> findBySha512(byte[] sha512) {
        return cacheSingle(mediaService.findBySha512(sha512));
    }

    @Override
    public CompletableFuture<Media> findByUri(URI uri) {
        return cacheSingle(mediaService.findByUri(uri));
    }

    @Override
    public CompletableFuture<List<Media>> findByVideoSequenceName(String videoSequenceName) {
        return cacheMany(mediaService.findByVideoSequenceName(videoSequenceName));
    }

    @Override
    public CompletableFuture<List<Media>> findByVideoName(String videoName) {
        return cacheMany(mediaService.findByVideoName(videoName));
    }

    @Override
    public CompletableFuture<List<String>> findAllVideoSequenceNames() {
        return mediaService.findAllVideoSequenceNames();
    }

    @Override
    public CompletableFuture<List<Media>> findByCameraIdAndTimestamp(String cameraId, Instant timestamp) {
        return cacheMany(mediaService.findByCameraIdAndTimestamp(cameraId, timestamp));

    }

    @Override
    public CompletableFuture<List<Media>> findByCameraIdAndDate(String cameraId, Instant startTimestamp, Instant endTimestamp) {
        return cacheMany(mediaService.findByCameraIdAndDate(cameraId, startTimestamp, endTimestamp));
    }

    @Override
    public CompletableFuture<List<Media>> findByVideoSequenceNameAndTimestamp(String videoSequenceName, Instant timestamp) {
        return cacheMany(mediaService.findByVideoSequenceNameAndTimestamp(videoSequenceName, timestamp));
    }

    @Override
    public CompletableFuture<List<String>> findAllCameraIds() {
        return mediaService.findAllCameraIds();
    }

    @Override
    public CompletableFuture<List<URI>> findAllURIs() {
        return mediaService.findAllURIs();
    }

    @Override
    public CompletableFuture<List<Media>> findConcurrentByVideoReferenceUuid(UUID uuid) {
        return cacheMany(mediaService.findConcurrentByVideoReferenceUuid(uuid));
    }

    @Override
    public CompletableFuture<List<Media>> findByFilename(String filename) {
        return cacheMany(mediaService.findByFilename(filename));

    }

    @Override
    public CompletableFuture<LastUpdate> findLastVideoSequenceUpdate(UUID uuid) {
        return mediaService.findLastVideoSequenceUpdate(uuid);
    }

    @Override
    public CompletableFuture<LastUpdate> findLastVideoUpdate(UUID uuid) {
        return mediaService.findLastVideoUpdate(uuid);
    }

    @Override
    public CompletableFuture<LastUpdate> findLastVideoReferenceUpdate(UUID uuid) {
        return mediaService.findLastVideoReferenceUpdate(uuid);
    }

    @Override
    public CompletableFuture<List<String>> findVideoSequenceNamesByCameraId(String cameraId) {
        return mediaService.findVideoSequenceNamesByCameraId(cameraId);
    }

    @Override
    public CompletableFuture<List<String>> findVideoNamesByVideoSequenceName(String videoSequenceName) {
        return mediaService.findVideoNamesByVideoSequenceName(videoSequenceName);
    }

    @Override
    public CompletableFuture<List<Media>> listVideoSequences(int pageNumber, int pageSize) {
        return mediaService.listVideoSequences(pageNumber, pageSize);
    }

    private CompletableFuture<List<Media>> cacheMany(CompletableFuture<List<Media>> future) {
        return future.thenApply(xs -> {
            if (xs != null) {
                var cache = mediaCache.synchronous();
                xs.forEach(m -> cache.put(m.getVideoReferenceUuid(), Optional.of(m)));
            }
            return xs;
        });
    }

    private CompletableFuture<Media> cacheSingle(CompletableFuture<Media> future) {
        return future.thenApply(m -> {
            if (m != null) {
                mediaCache.synchronous().put(m.getVideoReferenceUuid(), Optional.of(m));
            }
            return m;
        });
    }

//    private CompletableFuture<Media> invalidateSingle(CompletableFuture<Media> future) {
//        return future.thenApply(m -> {
//            if (m != null) {
//                mediaCache.synchronous().invalidate(m.getVideoReferenceUuid());
//            }
//            return m;
//        });
//    }
}
