/*
 * @(#)AnnotationService.java   2019.08.21 at 03:42:13 PDT
 *
 * Copyright 2011 MBARI
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mbari.vars.services;

import java.net.URL;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.UUID;
import org.mbari.vars.services.model.*;

/**
 *
 * @author Brian Schlining
 * @since 2017-05-11T15:41:00
 */
public interface AnnotationService {

        CompletableFuture<AnnotationCount> countAnnotations(UUID videoReferenceUuid);

        CompletableFuture<List<AnnotationCount>> countAnnotationsGroupByVideoReferenceUuid();

        CompletableFuture<ConcurrentRequestCount> countByConcurrentRequest(ConcurrentRequest concurrentRequest);

        CompletableFuture<MultiRequestCount> countByMultiRequest(MultiRequest multiRequest);

        CompletableFuture<List<AnnotationCount>> countImagedMomentsGroupByVideoReferenceUuid();

        CompletableFuture<ConceptCount> countObservationsByConcept(String concept);

        CompletableFuture<AnnotationCount> countImagedMomentsModifiedBefore(UUID videoReferenceUuid, Instant date);

        CompletableFuture<Annotation> createAnnotation(Annotation annotation);

        CompletableFuture<Collection<Annotation>> createAnnotations(Collection<Annotation> annotations);

        CompletableFuture<Association> createAssociation(UUID observationUuid, Association association);

        CompletableFuture<Image> createImage(Image image);

        CompletableFuture<List<AncillaryData>> createOrUpdateAncillaryData(List<AncillaryData> ancillaryData);

        CompletableFuture<AncillaryDataDeleteCount> deleteAncillaryDataByVideoReference(
                UUID videoReferenceUuid);

        CompletableFuture<Boolean> deleteAnnotation(UUID observationUuid);

        CompletableFuture<Void> deleteAnnotations(Collection<UUID> observationUuids);

        CompletableFuture<Boolean> deleteAssociation(UUID associationUuid);

        CompletableFuture<Void> deleteAssociations(Collection<UUID> associationUuids);

        CompletableFuture<Boolean> deleteImage(UUID imageReferenceUuid);

        CompletableFuture<List<String>> findActivities();

        CompletableFuture<List<UUID>> findAllVideoReferenceUuids();

        CompletableFuture<AncillaryData> findAncillaryData(UUID observationUuid);

        CompletableFuture<List<AncillaryData>> findAncillaryDataByVideoReference(
                UUID videoReferenceUuid);

        CompletableFuture<List<Annotation>> findAnnotations(UUID videoReferenceUuid);

        CompletableFuture<List<Annotation>> findAnnotations(UUID videoReferenceUuid, Long limit, Long offset);

        CompletableFuture<Association> findAssociationByUuid(UUID associationUuid);

        CompletableFuture<ConceptAssociationResponse> findByConceptAssociationRequest(
                        ConceptAssociationRequest request);

        CompletableFuture<List<Annotation>> findByConcurrentRequest(ConcurrentRequest concurrentRequest, long limit,
                        long offset);

        CompletableFuture<List<Annotation>> findByImageReference(UUID imageReferenceUuid);

        CompletableFuture<List<Annotation>> findByMultiRequest(MultiRequest multiRequest, long limit, long offset);

        CompletableFuture<Annotation> findByUuid(UUID observationUuid);

        CompletableFuture<List<Association>> findByVideoReferenceAndLinkName(UUID videoReferenceUuid, String linkName);

        CompletableFuture<List<Association>> findByVideoReferenceAndLinkNameAndConcept(UUID videoReferenceUuid,
                        String linkName, String concept);

        CompletableFuture<List<String>> findGroups();

        CompletableFuture<Image> findImageByUrl(URL url);

        CompletableFuture<Image> findImageByUuid(UUID imageReferenceUuid);

        CompletableFuture<List<Image>> findImagesByVideoReferenceUuid(UUID videoReferenceUuid);

        CompletableFuture<List<ImagedMoment>> findImagedMomentsByVideoReferenceUuid(UUID videoReferenceUuid);

        CompletableFuture<List<Index>> findIndicesByVideoReferenceUuid(UUID videoReferenceUuid);

        CompletableFuture<Collection<AncillaryData>> merge(UUID videoReferenceUuid, Collection<AncillaryData> data);

        CompletableFuture<ConceptsRenamed> renameConcepts(String oldConcept, String newConcept);

        CompletableFuture<Annotation> updateAnnotation(Annotation annotation);

        CompletableFuture<Collection<Annotation>> updateAnnotations(Collection<Annotation> annotations);

        CompletableFuture<Association> updateAssociation(Association association);

        CompletableFuture<Collection<Association>> updateAssociations(Collection<Association> associations);

        CompletableFuture<Image> updateImage(Image image);

        CompletableFuture<List<Index>> updateIndexRecordedTimestamps(Collection<Index> indices);

        CompletableFuture<Collection<Annotation>> updateRecordedTimestampsForTapes(Collection<Annotation> annotations);

        CompletableFuture<Optional<ImagedMoment>> updateRecordedTimestamp(UUID imagedMomentUuid, Instant recordedTimestamp);
}
