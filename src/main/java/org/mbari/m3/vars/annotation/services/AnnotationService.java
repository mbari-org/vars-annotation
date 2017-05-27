package org.mbari.m3.vars.annotation.services;

import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.AnnotationCount;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.m3.vars.annotation.model.Image;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 *
 * @author Brian Schlining
 * @since 2017-05-11T15:41:00
 */
public interface AnnotationService {

    CompletableFuture<Annotation> findByUuid(UUID observationUuid);

    CompletableFuture<List<Annotation>> findAnnotations(UUID videoReferenceUuid);

    CompletableFuture<List<Annotation>> findAnnotations(UUID videoReferenceUuid, Long limit, Long offset);

    CompletableFuture<List<Annotation>> findByImageReference(UUID imageReferenceUuid);

    CompletableFuture<AnnotationCount> countAnnotations(UUID videoReferenceUuid);

    CompletableFuture<Annotation> createAnnotation(Annotation annotation);

    CompletableFuture<Association> createAssociation(UUID observationUuid, Association association);

    CompletableFuture<Image> createImage(Image image);

    CompletableFuture<Annotation> updateAnnotation(Annotation annotation);

    CompletableFuture<Association> updateAssociation(Association association);

    CompletableFuture<Image> updateImage(Image image);

    CompletableFuture<Boolean> deleteAnnotation(UUID observationUuid);

    CompletableFuture<Boolean> deleteAssociation(UUID associationUuid);

    CompletableFuture<Boolean> deleteImage(UUID imageUuid);

}
