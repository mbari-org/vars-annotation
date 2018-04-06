package org.mbari.m3.vars.annotation.services;

import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.AnnotationCount;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2018-04-05T15:16:00
 */
public class CombinedMediaAnnotationDecorator {

    private final MediaService mediaService;
    private final AnnotationService annotationService;
    private final int chunkSize;

    public CombinedMediaAnnotationDecorator(UIToolBox toolBox) {
        this.mediaService = toolBox.getServices().getMediaService();
        this.annotationService = toolBox.getServices().getAnnotationService();
        this.chunkSize = toolBox.getConfig().getInt("annotation.service.chunk.size");
    }

    /**
     * Retrieve all annotations in a video sequence
     * @param videoSequenceName The name of the video sequence to lookup
     * @return All annotations from all video references in a video sequence
     */
    public CompletableFuture<List<Annotation>> findAllAnnotationsInDeployment(String videoSequenceName) {

        // Storage of annotations as futures complete. We'll flatten it later
        List<List<Annotation>> listsOfAnnotations = new ArrayList<>();

        // Store futures so that we cn wait on all of them to complete
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        mediaService.findByVideoSequenceName(videoSequenceName)
                .thenAccept(medias -> medias.forEach(media -> {
                            CompletableFuture<Void> f = annotationService.findAnnotations(media.getVideoReferenceUuid())
                                    .thenAccept(listsOfAnnotations::add);
                            futures.add(f);
                        }));

        CompletableFuture[] cfs = futures.toArray(new CompletableFuture[futures.size()]);
        return CompletableFuture.allOf(cfs)
                .thenApply(done -> listsOfAnnotations.stream()
                        .flatMap(List::stream)
                        .collect(Collectors.toList()));
    }

    /**
     * Find all annotations for a video reference. Using paging under the hood
     * so as not to time out the server when loading large annotation sets
     * @param videoReferenceUuid
     * @return All annotations for a video reference
     */
    public CompletableFuture<List<Annotation>> findAnnotations(UUID videoReferenceUuid) {
        CompletableFuture<List<Annotation>> cf = new CompletableFuture<>();
        annotationService.countAnnotations(videoReferenceUuid)
                .thenApply(ac ->  loadAnnotationPages(ac)
                    .thenApply(cf::complete));
        return cf;
    }

    /**
     * Loads all annotations for video reference using load pages under the hood
     * so as not to time out on the server with large annotation sets
     * @param ac
     * @return
     */
    private CompletableFuture<List<Annotation>> loadAnnotationPages(AnnotationCount ac) {

        // Storage of annotations as futures complete. We'll flatten it later
        List<List<Annotation>> listsOfAnnotations = new ArrayList<>();

        // Store futures so that we cn wait on all of them to complete
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Iterate over pages of annotations making individual calls
        int n = (int) Math.ceil(ac.getCount() / (double) chunkSize);
        for (int i = 0; i < n; i++) {
            long offset = i * chunkSize;
            long limit = chunkSize;
            CompletableFuture<Void> future = annotationService.findAnnotations(ac.getVideoReferenceUuid(), limit, offset)
                    .thenAccept(listsOfAnnotations::add);
            futures.add(future);

        }

        // Combine and flatten results.
        CompletableFuture[] cfs = futures.toArray(new CompletableFuture[futures.size()]);
        return CompletableFuture.allOf(cfs)
                .thenApply(done -> listsOfAnnotations.stream()
                        .flatMap(List::stream)
                        .collect(Collectors.toList()));

    }
}
