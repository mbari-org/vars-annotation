package org.mbari.vars.annotation.ui.services;

import org.mbari.vars.annosaurus.sdk.r1.models.AnnotationCount;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annosaurus.sdk.r1.AnnotationService;
import org.mbari.vars.vampiresquid.sdk.r1.MediaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * @author Brian Schlining
 * @since 2018-04-05T15:16:00
 */
public class CombinedMediaAnnotationDecorator {

    private final UIToolBox toolBox;
    private final int chunkSize;
    private final Duration chunkTimeout;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public CombinedMediaAnnotationDecorator(UIToolBox toolBox) {
        this.toolBox = toolBox;
        this.chunkSize = toolBox.getConfig().getInt("annotation.service.chunk.size");
        this.chunkTimeout = toolBox.getConfig().getDuration("annotation.service.timeout");
    }



    /**
     * Retrieve all annotations in a video sequence
     * @param videoSequenceName The name of the video sequence to lookup
     * @return All annotations from all video references in a video sequence
     */
    public CompletableFuture<List<Annotation>>  findAllAnnotationsInDeployment(String videoSequenceName) {

        CompletableFuture<List<Annotation>> cf = new CompletableFuture<>();

        MediaService mediaService = toolBox.getServices().mediaService();
        AnnotationService annotationService = toolBox.getServices().annotationService();

        // Store futures so that we cn wait on all of them to complete
        mediaService.findByVideoSequenceName(videoSequenceName)
                .thenAccept(medias -> {

                    // Storage of annotations as futures complete
                    List<Annotation> allAnnotations = new ArrayList<>();

                    medias.forEach(media -> {
                        CompletableFuture<List<Annotation>> f = annotationService.findAnnotations(media.getVideoReferenceUuid());
                        try {
                            List<Annotation> annotations = f.get(chunkTimeout.toMillis(), TimeUnit.MILLISECONDS);
                            allAnnotations.addAll(annotations);
                        } catch (Exception e) {
                            log.warn("Failed to load some annotations", e);
                        }
                    });

                    cf.complete(allAnnotations);

                });

        return cf;
    }

    /**
     * Find all annotations for a video reference. Using paging under the hood
     * so as not to time out the server when loading large annotation sets
     * @param videoReferenceUuid
     * @return All annotations for a video reference
     */
    public CompletableFuture<List<Annotation>> findAnnotations(UUID videoReferenceUuid) {
        CompletableFuture<List<Annotation>> cf = new CompletableFuture<>();
        AnnotationService annotationService = toolBox.getServices().annotationService();
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

        CompletableFuture<List<Annotation>> cf = new CompletableFuture<>();
        AnnotationService annotationService = toolBox.getServices().annotationService();

        Runnable task = () -> {
            // Iterate over pages of annotations making individual calls
            List<Annotation> allAnnotations = new ArrayList<>();
            int n = (int) Math.ceil(ac.getCount() / (double) chunkSize);
            for (int i = 0; i < n; i++) {
                long offset = i * chunkSize;
                long limit = chunkSize;

                try {
                    List<Annotation> annotations = annotationService
                            .findAnnotations(ac.getVideoReferenceUuid(), limit, offset, false)
                            .get(chunkTimeout.toMillis(), TimeUnit.MILLISECONDS);
                    allAnnotations.addAll(annotations);
                }
                catch (Exception e) {
                    log.warn("Failed to load page chunk (" + offset + " to " +
                            offset + limit + ")", e);
                }
            }
            cf.complete(allAnnotations);
        };

        toolBox.getExecutorService().submit(task);

        return cf;

    }
}
