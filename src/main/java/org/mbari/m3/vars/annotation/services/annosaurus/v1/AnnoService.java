package org.mbari.m3.vars.annotation.services.annosaurus.v1;

import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.m3.vars.annotation.model.Authorization;
import org.mbari.m3.vars.annotation.model.Image;
import org.mbari.m3.vars.annotation.services.AnnotationService;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Brian Schlining
 * @since 2017-05-23T10:09:00
 */
public class AnnoService implements AnnotationService {

    private final AnnotationWebService service;

    public AnnoService(ServiceGenerator serviceGenerator, Authorization authorization) {
        service = serviceGenerator.create(AnnotationWebService.class, authorization);
    }

    @Override
    public CompletableFuture<List<Annotation>> findAnnotations(UUID videoReferenceUuid) {
        return null;
    }

    @Override
    public CompletableFuture<List<Annotation>> findAnnotations(UUID videoReferenceUuid, int limit, int offset) {
        return null;
    }

    @Override
    public CompletableFuture<List<Annotation>> countAnnotations(UUID videoReferenceUuid) {
        return null;
    }

    @Override
    public CompletableFuture<Annotation> createAnnotation(Annotation annotation) {
        return null;
    }


    @Override
    public CompletableFuture<Annotation> createAssociation(UUID observationUuid, Association association) {
        return null;
    }

    @Override
    public CompletableFuture<Image> createImage(Image image) {
        return null;
    }

    @Override
    public CompletableFuture<Annotation> updateAnnotation(Annotation annotation) {
        return null;
    }

    @Override
    public CompletableFuture<Annotation> updateAssociation(Association association) {
        return null;
    }

    @Override
    public CompletableFuture<Image> updateImage(Image image) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> deleteAnnotation(UUID observationUuid) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> deleteAssociation(UUID associationUuid) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> deleteImage(UUID imageUuid) {
        return null;
    }


}
