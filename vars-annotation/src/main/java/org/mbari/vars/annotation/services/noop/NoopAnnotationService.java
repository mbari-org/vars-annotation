package org.mbari.vars.annotation.services.noop;

import org.mbari.vars.annosaurus.sdk.r1.AnnotationService;
import org.mbari.vars.annosaurus.sdk.r1.models.*;

import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class NoopAnnotationService implements AnnotationService {
    @Override
    public CompletableFuture<AnnotationCount> countAnnotations(UUID videoReferenceUuid) {
        return CompletableFuture.completedFuture(new AnnotationCount(videoReferenceUuid, 0));
    }

    @Override
    public CompletableFuture<List<AnnotationCount>> countAnnotationsGroupByVideoReferenceUuid() {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<ConcurrentRequestCount> countByConcurrentRequest(ConcurrentRequest concurrentRequest) {
        return CompletableFuture.completedFuture(new ConcurrentRequestCount(concurrentRequest, 0L));
    }

    @Override
    public CompletableFuture<MultiRequestCount> countByMultiRequest(MultiRequest multiRequest) {
        return CompletableFuture.completedFuture(new MultiRequestCount(multiRequest, 0L));
    }

    @Override
    public CompletableFuture<Count> countImagesByVideoReferenceUuid(UUID videoReferenceUuid) {
        return null;
    }

    @Override
    public CompletableFuture<List<AnnotationCount>> countImagedMomentsGroupByVideoReferenceUuid() {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<ConceptCount> countObservationsByConcept(String concept) {
        return CompletableFuture.completedFuture(new ConceptCount(concept, 0));
    }

    @Override
    public CompletableFuture<AnnotationCount> countImagedMomentsModifiedBefore(UUID videoReferenceUuid, Instant date) {
        return CompletableFuture.completedFuture(new AnnotationCount(videoReferenceUuid, 0));
    }

    @Override
    public CompletableFuture<Annotation> createAnnotation(Annotation annotation) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Collection<Annotation>> createAnnotations(Collection<Annotation> annotations) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Association> createAssociation(UUID observationUuid, Association association) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Association> createAssociation(UUID observationUuid, Association association, UUID associationUuid) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Image> createImage(Image image) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<List<AncillaryData>> createOrUpdateAncillaryData(List<AncillaryData> ancillaryData) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<CachedVideoReference> createCachedVideoReference(CachedVideoReference cvr) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<AncillaryDataDeleteCount> deleteAncillaryDataByVideoReference(UUID videoReferenceUuid) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Boolean> deleteAnnotation(UUID observationUuid) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Boolean> deleteAnnotations(Collection<UUID> observationUuids) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<DeleteCount> deleteAnnotationsByVideoReferenceUuid(UUID videoReferenceUuid) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Boolean> deleteAssociation(UUID associationUuid) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Boolean> deleteAssociations(Collection<UUID> associationUuids) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Boolean> deleteImage(UUID imageReferenceUuid) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Annotation> deleteDuration(UUID observationUuid) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Boolean> deleteCacheVideoReference(UUID uuid) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<List<String>> findActivities() {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<UUID>> findAllVideoReferenceUuids() {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<AncillaryData> findAncillaryData(UUID observationUuid) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<List<AncillaryData>> findAncillaryDataByVideoReference(UUID videoReferenceUuid) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<Annotation>> findByConcept(String concept, Boolean data) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<Annotation>> findByConcept(String concept, Long limit, Long offset, Boolean data) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<Annotation>> findAnnotations(UUID videoReferenceUuid) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<Annotation>> findAnnotations(UUID videoReferenceUuid, boolean data) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<Annotation>> findAnnotations(UUID videoReferenceUuid, Long limit, Long offset) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<Annotation>> findAnnotations(UUID videoReferenceUuid, Long limit, Long offset, Boolean data) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<Association> findAssociationByUuid(UUID associationUuid) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<ConceptAssociationResponse> findByConceptAssociationRequest(ConceptAssociationRequest request) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<List<Annotation>> findByConcurrentRequest(ConcurrentRequest concurrentRequest, long limit, long offset) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<Annotation>> findByImageReference(UUID imageReferenceUuid) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<Annotation>> findByMultiRequest(MultiRequest multiRequest, long limit, long offset) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<Annotation> findByUuid(UUID observationUuid) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<List<Association>> findByVideoReferenceAndLinkName(UUID videoReferenceUuid, String linkName) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<Association>> findByVideoReferenceAndLinkNameAndConcept(UUID videoReferenceUuid, String linkName, String concept) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<String>> findGroups() {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<Image> findImageByUrl(URL url) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Image> findImageByUuid(UUID imageReferenceUuid) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<List<Image>> findImagesByVideoReferenceUuid(UUID videoReferenceUuid) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<Image>> findImagesByVideoReferenceUuid(UUID videoReferenceUuid, Long limit, Long offset) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<List<ImagedMoment>> findImagedMomentsByVideoReferenceUuid(UUID videoReferenceUuid) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<Index>> findIndicesByVideoReferenceUuid(UUID videoReferenceUuid) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<CachedVideoReference> findVideoReferenceByVideoReferenceUuid(UUID videoReferenceUuid) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Collection<AncillaryData>> merge(UUID videoReferenceUuid, Collection<AncillaryData> data) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<ConceptsRenamed> renameConcepts(String oldConcept, String newConcept) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<ConceptsRenamed> renameToConcepts(String oldConcept, String newConcept) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Annotation> updateAnnotation(Annotation annotation) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Collection<Annotation>> updateAnnotations(Collection<Annotation> annotations) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Association> updateAssociation(Association association) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Collection<Association>> updateAssociations(Collection<Association> associations) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Image> updateImage(Image image) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<List<Index>> updateIndexRecordedTimestamps(Collection<Index> indices) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Count> updateObservations(ObservationsUpdate update) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Collection<Annotation>> updateRecordedTimestampsForTapes(Collection<Annotation> annotations) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Optional<Index>> updateRecordedTimestamp(UUID imagedMomentUuid, Instant recordedTimestamp) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<CachedVideoReference> updateCachedVideoReference(CachedVideoReference cvr) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }
}
