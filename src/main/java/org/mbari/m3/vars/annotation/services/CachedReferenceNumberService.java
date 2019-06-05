package org.mbari.m3.vars.annotation.services;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.mbari.m3.vars.annotation.UIToolBox;
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

    private static class OldNumberKey {
        private final Media media;
        private final String concept;

        public OldNumberKey(Media media, String concept) {
            this.media = media;
            this.concept = concept;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OldNumberKey that = (OldNumberKey) o;
            return media.equals(that.media) &&
                    concept.equals(that.concept);
        }

        @Override
        public int hashCode() {
            return Objects.hash(media, concept);
        }
    }


    private final UIToolBox toolBox;
    private final AnnotationService annotationService;
    private final MediaService mediaService;
    private final List<Media> medias = new CopyOnWriteArrayList<>();
    private final List<Association> remoteNewNumbers = new CopyOnWriteArrayList<>();
    private final AsyncLoadingCache<String, List<Association>> oldNumberCache;
    private final String associationKey;


    public CachedReferenceNumberService(UIToolBox toolBox) {
        this.toolBox = toolBox;
        this.annotationService = toolBox.getServices().getAnnotationService();
        this.mediaService = toolBox.getServices().getMediaService();
        associationKey = toolBox.getConfig()
                .getString("app.annotation.identity.reference");

        oldNumberCache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(60))
                .buildAsync((key, executor) -> {})

    }

    public synchronized void clear() {

    }

    public CompletableFuture<List<Media>> findRelatedMedia(Media media) {
        if (!medias.isEmpty() && !medias.contains(media)) {
            clear();
        }

        if (medias.isEmpty()) {
           return  mediaService.findByVideoSequenceName(media.getVideoSequenceName())
                    .thenApply(ms -> {
                       medias.addAll(ms);
                       return ms;
                    });
        }
        else {
            return CompletableFuture.completedFuture(medias);
        }
    }

    public CompletableFuture<List<Association>> findRemoteNewNumbers(Media media) {
        return findRelatedMedia(media)
                .thenCompose(ms -> {
                    if (remoteNewNumbers.isEmpty()) {
                        return AsyncUtils.collectAll(ms, m ->
                                annotationService.findByVideoReferenceAndLinkName(m.getVideoReferenceUuid(), associationKey))
                            .thenApply(associationLists ->
                                associationLists.stream()
                                        .flatMap(List::stream)
                                        .collect(Collectors.toList()));
                    }
                    else {
                        return CompletableFuture.completedFuture(remoteNewNumbers);
                    }
                });
    }

    public CompletableFuture<List<Association>> findNewReferenceNumbers(Media media) {
        return findRemoteNewNumbers(media)
                .thenApply(associations -> {
                    associations.addAll(currentReferences());
                    return associations.stream()
                            .sorted(Comparator.comparing(Association::getLinkValue))
                            .collect(Collectors.toList());
                });
    }

    private List<Association> currentReferences() {
        return toolBox.getData()
                .getAnnotations()
                .stream()
                .flatMap(a -> a.getAssociations().stream())
                .filter(a -> a.getLinkName().equals(associationKey))
                .collect(Collectors.toList());
    }

    private List<Association> currentReferences(String concept) {
        return toolBox.getData()
                .getAnnotations()
                .stream()
                .filter(a -> a.getConcept().equals(concept))
                .flatMap(a -> a.getAssociations().stream())
                .filter(a -> a.getLinkName().equals(associationKey))
                .collect(Collectors.toList());
    }

    private CompletableFuture<List<Association>> loadOldNumbers(Media media,
                                                                  String concept) {
        return findRelatedMedia(media)
                .thenCompose(ms -> AsyncUtils.collectAll(medias,
                        m -> annotationService.findByVideoReferenceAndLinkNameAndConcept(m.getVideoReferenceUuid(),
                                associationKey,
                                concept)))
                .thenApply(associationLists ->
                     associationLists.stream()
                            .flatMap(List::stream)
                            .collect(Collectors.toList()));
    }
}
