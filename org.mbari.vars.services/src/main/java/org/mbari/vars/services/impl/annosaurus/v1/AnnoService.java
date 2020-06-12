/*
 * @(#)AnnoService.java   2019.08.21 at 04:01:23 PDT
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

package org.mbari.vars.services.impl.annosaurus.v1;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Named;
import org.mbari.vars.services.AnnotationService;
import org.mbari.vars.services.AuthService;
import org.mbari.vars.services.model.*;
import org.mbari.vars.services.RetrofitWebService;
import retrofit2.Call;

/**
 * @author Brian Schlining
 * @since 2017-05-23T10:09:00
 */
public class AnnoService implements AnnotationService, RetrofitWebService {

    private final AnnoWebService annoService;
    private final AssociationWebService assService;
    private final ImageWebService imageService;
    private final AncillaryDataWebService dataService;
    private final IndexWebService indexService;
    private final ImagedMomentWebService imagedMomentService;
    private final VideoInfoWebService videoInfoWebService;
    private final Map<String, String> defaultHeaders;
    private final Map<String, String> bulkHeaders;

    /**
     *
     * @param serviceFactory
     * @param authService
     */
    @Inject
    public AnnoService(AnnoWebServiceFactory serviceFactory,
            @Named("ANNO_AUTH") AuthService authService) {
        annoService = serviceFactory.create(AnnoWebService.class, authService);
        assService = serviceFactory.create(AssociationWebService.class, authService);
        imageService = serviceFactory.create(ImageWebService.class, authService);
        dataService = serviceFactory.create(AncillaryDataWebService.class, authService);
        indexService = serviceFactory.create(IndexWebService.class, authService);
        imagedMomentService = serviceFactory.create(ImagedMomentWebService.class, authService);
        videoInfoWebService = serviceFactory.create(VideoInfoWebService.class, authService);
        defaultHeaders = new HashMap<>();
        defaultHeaders.put("Accept", "application/json");
        defaultHeaders.put("Accept-Charset", "utf-8");
        bulkHeaders = new HashMap<>(defaultHeaders);
        bulkHeaders.put("Content-Type", "application/json");
    }

    private void addField(Map<String, String> map, String key, Object value) {
        if (value != null) {
            map.put(key, asString(value));
        }
    }

    @Override
    public CompletableFuture<AnnotationCount> countAnnotations(UUID videoReferenceUuid) {
        return sendRequest(annoService.countByVideoReferenceUuid(videoReferenceUuid));
    }

    @Override
    public CompletableFuture<List<AnnotationCount>> countAnnotationsGroupByVideoReferenceUuid() {
        return sendRequest(annoService.countAllGroupByVideoReferenceUuid());
    }

    @Override
    public CompletableFuture<List<Annotation>> findByConcept(String concept, Boolean data) {
        // TODO: add pager?
        // HACK: Hardcoded max of a milliion annotations
        return sendRequest(annoService.findByConcept(concept, 0L, 1000000L, data));
    }

    @Override
    public CompletableFuture<List<Annotation>> findByConcept(String concept, Long limit, Long offset, Boolean data) {
        return sendRequest(annoService.findByConcept(concept, limit, offset, data));
    }

    /**
     *
     * @param concurrentRequest
     * @return
     */
    public CompletableFuture<ConcurrentRequestCount> countByConcurrentRequest(
            ConcurrentRequest concurrentRequest) {
        return sendRequest(annoService.countByConcurrentRequest(concurrentRequest, bulkHeaders));
    }

    /**
     *
     * @param multiRequest
     * @return
     */
    public CompletableFuture<MultiRequestCount> countByMultiRequest(MultiRequest multiRequest) {
        return sendRequest(annoService.countByMultiRequest(multiRequest, bulkHeaders));
    }

    @Override
    public CompletableFuture<List<AnnotationCount>> countImagedMomentsGroupByVideoReferenceUuid() {
        return sendRequest(annoService.countImagedMomentsGroupByVideoReferenceUuid());
    }

    @Override
    public CompletableFuture<ConceptCount> countObservationsByConcept(String concept) {
        return sendRequest(annoService.countObservationsByConcept(concept));
    }

    @Override
    public CompletableFuture<AnnotationCount> countImagedMomentsModifiedBefore(UUID videoReferenceUuid, Instant date) {
        return sendRequest(imagedMomentService.countByModifiedBefore(videoReferenceUuid, date));
    }

    @Override
    public CompletableFuture<Annotation> createAnnotation(Annotation annotation) {
        Long durationMillis = (annotation.getDuration() == null) ? null : annotation.getDuration()
                .toMillis();
        Long elapsedTimeMilliis = (annotation.getElapsedTime() == null)
            ? null : annotation.getElapsedTime()
                    .toMillis();
        Call<Annotation> call = annoService.create(annotation.getVideoReferenceUuid(),
                annotation.getConcept(),
                annotation.getObserver(),
                annotation.getObservationTimestamp(),
                annotation.getTimecode(),
                elapsedTimeMilliis,
                annotation.getRecordedTimestamp(),
                durationMillis,
                annotation.getGroup(),
                annotation.getActivity(),
                defaultHeaders);

        return sendRequest(call);
    }

    /**
     *
     * @param annotations
     * @return
     */
    public CompletableFuture<Collection<Annotation>> createAnnotations(
            Collection<Annotation> annotations) {
        return sendRequest(annoService.create(annotations, bulkHeaders));
    }

    @Override
    public CompletableFuture<Association> createAssociation(UUID observationUuid,
            Association association) {
        Call<Association> call = assService.create(observationUuid,
                association.getLinkName(),
                association.getToConcept(),
                association.getLinkValue(),
                association.getMimeType(),
                defaultHeaders);

        return sendRequest(call);
    }

    /**
     * Creates an association using a predefined uuid for it. This method was added to
     * support bounding boxes created by Cthulhu which uses the associationUuid as
     * identity for bounding boxes.
     * @param observationUuid
     * @param association
     * @param associationUuid
     * @return
     */
    @Override
    public CompletableFuture<Association> createAssociation(UUID observationUuid,
                                                            Association association,
                                                            UUID associationUuid) {
        Call<Association> call = assService.create(observationUuid,
                association.getLinkName(),
                association.getToConcept(),
                association.getLinkValue(),
                association.getMimeType(),
                associationUuid,
                defaultHeaders);

        return sendRequest(call);
    }

    @Override
    public CompletableFuture<Image> createImage(Image image) {
        String url = (image.getUrl() == null) ? null : image.getUrl()
                .toExternalForm();
        String timecode = asString(image.getTimecode());
        Long elapsedTimeMillis = (image.getElapsedTime() == null) ? null : image.getElapsedTime()
                .toMillis();

        Call<Image> call = imageService.create(image.getVideoReferenceUuid(),
                url,
                timecode,
                elapsedTimeMillis,
                image.getRecordedTimestamp(),
                image.getFormat(),
                image.getWidth(),
                image.getHeight(),
                image.getDescription(),
                defaultHeaders);

        return sendRequest(call);
    }

    /**
     *
     * @param ancillaryData
     * @return
     */
    public CompletableFuture<List<AncillaryData>> createOrUpdateAncillaryData(
            List<AncillaryData> ancillaryData) {
        return sendRequest(dataService.createOrUpdate(ancillaryData, bulkHeaders));
    }

    /**
     *
     * @param videoReferenceUuid
     * @return
     */
    @Override
    public CompletableFuture<AncillaryDataDeleteCount> deleteAncillaryDataByVideoReference(
            UUID videoReferenceUuid) {
        return sendRequest(dataService.deleteByVideoReference(videoReferenceUuid));
    }

    @Override
    public CompletableFuture<Boolean> deleteAnnotation(UUID observationUuid) {
        return sendRequest(annoService.delete(observationUuid, defaultHeaders));
    }

    /**
     *
     * @param observationUuids
     * @return
     */
    public CompletableFuture<Void> deleteAnnotations(Collection<UUID> observationUuids) {
        return sendRequest(annoService.delete(observationUuids, bulkHeaders));
    }

    @Override
    public CompletableFuture<Boolean> deleteAssociation(UUID associationUuid) {
        return sendRequest(assService.delete(associationUuid));
    }

    /**
     *
     * @param associationUuids
     * @return
     */
    public CompletableFuture<Void> deleteAssociations(Collection<UUID> associationUuids) {
        return sendRequest(assService.delete(associationUuids, bulkHeaders));
    }

    @Override
    public CompletableFuture<Boolean> deleteImage(UUID imageReferenceUuid) {
        return sendRequest(imageService.delete(imageReferenceUuid));
    }

    @Override
    public CompletableFuture<Annotation> deleteDuration(UUID observationUuid) {
        return sendRequest(annoService.deleteDuration(observationUuid))
                .thenCompose(observation -> findByUuid(observation.getUuid()));
    }

    @Override
    public CompletableFuture<List<String>> findActivities() {
        return sendRequest(annoService.findActivities());
    }

    @Override
    public CompletableFuture<List<UUID>> findAllVideoReferenceUuids() {
        return sendRequest(annoService.findAllVideoReferenceUuids());
    }

    @Override
    public CompletableFuture<AncillaryData> findAncillaryData(UUID observationUuid) {
        return sendRequest(annoService.findAncillaryData(observationUuid));
    }

    /**
     *
     * @param videoReferenceUuid
     * @return
     */
    @Override
    public CompletableFuture<List<AncillaryData>> findAncillaryDataByVideoReference(
            UUID videoReferenceUuid) {
        return sendRequest(dataService.findByVideoReferenceUuid(videoReferenceUuid));
    }

    @Override
    public CompletableFuture<List<Annotation>> findAnnotations(UUID videoReferenceUuid) {
        return findAnnotations(videoReferenceUuid, null, null);
    }


    @Override
    public CompletableFuture<List<Annotation>> findAnnotations(UUID videoReferenceUuid, Long limit,
            Long offset) {
        return sendRequest(annoService.findByVideoReferenceUuid(videoReferenceUuid, limit, offset));
    }

    /**
     *
     * @param associationUuid
     * @return
     */
    public CompletableFuture<Association> findAssociationByUuid(UUID associationUuid) {
        return sendRequest(assService.findByUuid(associationUuid));
    }

    /**
     *
     * @param request
     * @return
     */
    public CompletableFuture<ConceptAssociationResponse> findByConceptAssociationRequest(
            ConceptAssociationRequest request) {
        return sendRequest(assService.findByConceptAssociationRequest(request, bulkHeaders));
    }

    /**
     *
     * @param concurrentRequest
     * @param limit
     * @param offset
     * @return
     */
    public CompletableFuture<List<Annotation>> findByConcurrentRequest(
            ConcurrentRequest concurrentRequest, long limit, long offset) {
        return sendRequest(annoService.findByConcurrentRequest(concurrentRequest,
                limit,
                offset,
                bulkHeaders));
    }

    /**
     *
     * @param imageReferenceUuid
     * @return
     */
    public CompletableFuture<List<Annotation>> findByImageReference(UUID imageReferenceUuid) {
        return sendRequest(annoService.findByImageReferenceUuid(imageReferenceUuid));
    }

    /**
     *
     * @param multiRequest
     * @param limit
     * @param offset
     * @return
     */
    public CompletableFuture<List<Annotation>> findByMultiRequest(MultiRequest multiRequest,
            long limit, long offset) {
        return sendRequest(annoService.findByMultiRequest(multiRequest,
                limit,
                offset,
                bulkHeaders));
    }

    @Override
    public CompletableFuture<Annotation> findByUuid(UUID observationUuid) {
        return sendRequest(annoService.findByUuid(observationUuid));
    }

    /**
     *
     * @param videoReferenceUuid
     * @param linkName
     * @return
     */
    public CompletableFuture<List<Association>> findByVideoReferenceAndLinkName(
            UUID videoReferenceUuid, String linkName) {
        return sendRequest(assService.findByVideoReferenceAndLinkName(videoReferenceUuid,
                linkName));
    }

    /**
     *
     * @param videoReferenceUuid
     * @param linkName
     * @param concept
     * @return
     */
    public CompletableFuture<List<Association>> findByVideoReferenceAndLinkNameAndConcept(
            UUID videoReferenceUuid, String linkName, String concept) {
        return sendRequest(assService.findByVideoReferenceAndLinkNameAndConcept(videoReferenceUuid,
                linkName,
                concept));
    }

    @Override
    public CompletableFuture<List<String>> findGroups() {
        return sendRequest(annoService.findGroups());
    }

    /**
     *
     * @param url
     * @return
     */
    public CompletableFuture<Image> findImageByUrl(URL url) {
        return sendRequest(imageService.findByUrl(url.toExternalForm()));
    }

    /**
     *
     * @param imageReferenceUuid
     * @return
     */
    public CompletableFuture<Image> findImageByUuid(UUID imageReferenceUuid) {
        return sendRequest(imageService.findByUuid(imageReferenceUuid));
    }

    @Override
    public CompletableFuture<List<Image>> findImagesByVideoReferenceUuid(UUID videoReferenceUuid) {
        return sendRequest(imageService.findByVideoReferenceUuid(videoReferenceUuid));
    }

    @Override
    public CompletableFuture<List<ImagedMoment>> findImagedMomentsByVideoReferenceUuid(UUID videoReferenceUuid) {
        return sendRequest(imagedMomentService.findByVideoReferenceUuid(videoReferenceUuid));
    }

    /**
     *
     * @param videoReferenceUuid
     * @return
     */
    public CompletableFuture<List<Index>> findIndicesByVideoReferenceUuid(UUID videoReferenceUuid) {
        return sendRequest(indexService.findByVideoReferenceUuid(videoReferenceUuid));
    }

    @Override
    public CompletableFuture<CachedVideoReference> findVideoReferenceByVideoReferenceUuid(UUID videoReferenceUuid) {
        return sendRequest(videoInfoWebService.findByVideoReferenceUuid(videoReferenceUuid));
    }

    /**
     *
     * @param videoReferenceUuid
     * @param data
     * @return
     */
    public CompletableFuture<Collection<AncillaryData>> merge(UUID videoReferenceUuid,
            Collection<AncillaryData> data) {
        return sendRequest(annoService.merge(videoReferenceUuid, data, bulkHeaders));
    }

    /**
     *
     * @param oldConcept
     * @param newConcept
     * @return
     */
    public CompletableFuture<ConceptsRenamed> renameConcepts(String oldConcept, String newConcept) {
        return sendRequest(annoService.renameConcepts(oldConcept, newConcept, defaultHeaders));
    }

    @Override
    public CompletableFuture<Annotation> updateAnnotation(Annotation annotation) {
        Map<String, String> fieldMap = new HashMap<>();
        addField(fieldMap, "video_reference_uuid", annotation.getVideoReferenceUuid());
        addField(fieldMap, "concept", annotation.getConcept());
        addField(fieldMap, "observer", annotation.getObserver());
        addField(fieldMap, "timecode", annotation.getTimecode());
        Long elapsedTimeMillis = (annotation.getElapsedTime() == null)
            ? null : annotation.getElapsedTime()
                    .toMillis();
        addField(fieldMap, "elapse_time_millis", elapsedTimeMillis);
        addField(fieldMap, "recorded_timestamp", annotation.getRecordedTimestamp());
        Long durationMillis = (annotation.getDuration() == null) ? null : annotation.getDuration()
                .toMillis();
        addField(fieldMap, "duration_millis", durationMillis);
        addField(fieldMap, "group", annotation.getGroup());
        addField(fieldMap, "activity", annotation.getActivity());

        return sendRequest(annoService.update(annotation.getObservationUuid(),
                fieldMap,
                defaultHeaders));
    }

    /**
     *
     * @param annotations
     * @return
     */
    public CompletableFuture<Collection<Annotation>> updateAnnotations(
            Collection<Annotation> annotations) {
        return sendRequest(annoService.update(annotations, bulkHeaders));
    }

    @Override
    public CompletableFuture<Association> updateAssociation(Association association) {
        Map<String, String> fieldMap = new HashMap<>();
        addField(fieldMap, "link_name", association.getLinkName());
        addField(fieldMap, "to_concept", association.getToConcept());
        addField(fieldMap, "link_value", association.getLinkValue());
        addField(fieldMap, "mime_type", association.getMimeType());

        return sendRequest(assService.update(association.getUuid(), fieldMap, defaultHeaders));
    }

    /**
     *
     * @param associations
     * @return
     */
    public CompletableFuture<Collection<Association>> updateAssociations(
            Collection<Association> associations) {
        return sendRequest(assService.update(associations));
    }

    @Override
    public CompletableFuture<Image> updateImage(Image image) {
        Map<String, String> fieldMap = new HashMap<>();
        addField(fieldMap, "video_reference_uuid", image.getVideoReferenceUuid());
        String url = (image.getUrl() == null) ? null : image.getUrl()
                .toExternalForm();
        addField(fieldMap, "url", url);
        addField(fieldMap, "timecode", image.getTimecode());
        Long elapsedTimeMillis = (image.getElapsedTime() == null) ? null : image.getElapsedTime()
                .toMillis();
        addField(fieldMap, "elapsed_time_millis", elapsedTimeMillis);
        addField(fieldMap, "recorded_timestamp", image.getRecordedTimestamp());
        addField(fieldMap, "format", image.getFormat());
        addField(fieldMap, "width_pixels", image.getWidth());
        addField(fieldMap, "height_pixels", image.getHeight());
        addField(fieldMap, "description", image.getDescription());

        return sendRequest(imageService.update(image.getImageReferenceUuid(),
                fieldMap,
                defaultHeaders));
    }

    /**
     *
     * @param indices
     * @return
     */
    @Override
    public CompletableFuture<List<Index>> updateIndexRecordedTimestamps(Collection<Index> indices) {
        return sendRequest(indexService.update(indices, bulkHeaders));
    }

    /**
     *
     * @param annotations
     * @return
     */
    @Override
    public CompletableFuture<Collection<Annotation>> updateRecordedTimestampsForTapes(
            Collection<Annotation> annotations) {
        return sendRequest(annoService.updateRecordedTimestampForTapes(annotations, bulkHeaders));
    }

    @Override
    public CompletableFuture<Optional<ImagedMoment>> updateRecordedTimestamp(UUID imagedMomentUuid, Instant recordedTimestamp) {
        Map<String, String> map = Map.of("recorded_timestamp", recordedTimestamp.toString());
        return sendRequest(imagedMomentService.update(imagedMomentUuid, map, defaultHeaders))
                .thenApply(Optional::ofNullable);

    }

}
