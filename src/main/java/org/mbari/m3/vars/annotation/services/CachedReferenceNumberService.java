package org.mbari.m3.vars.annotation.services;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.util.AsyncUtils;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2019-06-04T16:03:00
 */
public class CachedReferenceNumberService {

    private static class NewNumberKey {
        final Media media;
    }

    private final AnnotationService annotationService;
    private final MediaService mediaService;
    private final AsyncLoadingCache<Media, List<Media>> loadedMediaCache;
    private final AsyncLoadingCache<String, List<Association>> oldNumberCache;
    private final List<Association> oldNumbers = new CopyOnWriteArrayList<>();

    public CachedReferenceNumberService(AnnotationService annotationService,
                                        MediaService mediaService) {
        this.annotationService = annotationService;
        this.mediaService = mediaService;

        oldNumberCache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(60));

        loadedMediaCache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(120))
                .buildAsync((key, executor) -> mediaService.findByVideoSequenceName(key.getVideoSequenceName());
    }

    public synchronized void clear() {

    }

    public CompletableFuture<List<Media>> findRelatedMedia(Media media) {
        return loadedMediaCache.get(media);
    }

    private CompletableFuture<List<Association>> loadNewNumbers(Media media, String associationKey) {



        return mediaService.findByVideoSequenceName(media.getVideoSequenceName())
                .thenCompose(medias -> AsyncUtils.collectAll(medias, m ->
                        annotationService.findByVideoReferenceAndLinkName(m.getVideoReferenceUuid(), associationKey)))
                .thenApply(associationLists ->
                     associationLists.stream()
                            .flatMap(List::stream)
                            .collect(Collectors.toList()));

    }

    private CompletableFuture<Map<String, String>> loadOldNumbers(Media media,
                                                                  String associationKey,
                                                                  String concept) {
        mediaService.findByVideoSequenceName(media.getVideoSequenceName())
                .thenCompose(medias -> {
                    medias.forEach(m -> {
                        annotationService.findByVideoReferenceAndLinkNameAndConcept(m.getVideoReferenceUuid(), associationKey, concept)
                                .thenAccept(associations -> {
                                    Map<String, List<Association>> map = new HashMap<>();
                                    map.put(associationKey, )

                                    oldNumberCache.synchronous().put(media, )
                                })
                    });
                });
    }
}
