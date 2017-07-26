package org.mbari.m3.vars.annotation.services;

import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.AnnotationCount;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.m3.vars.annotation.model.Image;

import java.util.Collection;
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

    CompletableFuture<Image> findImageByUuid(UUID imageReferenceUuid);

    CompletableFuture<Association> findAssociationByUuid(UUID associationUuid);

    CompletableFuture<AnnotationCount> countAnnotations(UUID videoReferenceUuid);

    CompletableFuture<Annotation> createAnnotation(Annotation annotation);

    CompletableFuture<Collection<Annotation>> createAnnotations(Collection<Annotation> annotations);

    CompletableFuture<Association> createAssociation(UUID observationUuid, Association association);

    CompletableFuture<Image> createImage(Image image);

    CompletableFuture<Annotation> updateAnnotation(Annotation annotation);

    CompletableFuture<Collection<Annotation>> updateAnnotations(Collection<Annotation> annotations);

    CompletableFuture<Association> updateAssociation(Association association);

    CompletableFuture<Collection<Association>> updateAssociations(Collection<Association> associations);

    CompletableFuture<Image> updateImage(Image image);

    CompletableFuture<Boolean> deleteAnnotation(UUID observationUuid);

    CompletableFuture<Void> deleteAnnotations(Collection<UUID> observationUuids);

    CompletableFuture<Boolean> deleteAssociation(UUID associationUuid);

    CompletableFuture<Void> deleteAssociations(Collection<UUID> associationUuids);

    CompletableFuture<Boolean> deleteImage(UUID imageUuid);

}
