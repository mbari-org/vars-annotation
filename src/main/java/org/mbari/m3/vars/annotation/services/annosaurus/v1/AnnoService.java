package org.mbari.m3.vars.annotation.services.annosaurus.v1;

import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.m3.vars.annotation.model.Image;
import org.mbari.m3.vars.annotation.services.AnnotationService;
import org.mbari.m3.vars.annotation.services.AuthService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author Brian Schlining
 * @since 2017-05-23T10:09:00
 */
public class AnnoService implements AnnotationService {

    private final AnnotationWebService annoService;
    private final AssociationWebService assService;
    private final ImageWebService imageService;

    private final Map<String, String> defaultHeaders;

    @Inject
    public AnnoService(ServiceGenerator serviceGenerator, AuthService authService) {
        annoService = serviceGenerator.create(AnnotationWebService.class, authService);
        assService = serviceGenerator.create(AssociationWebService.class, authService);
        imageService = serviceGenerator.create(ImageWebService.class, authService);
        defaultHeaders = new HashMap<>();
        defaultHeaders.put("Accept", "application/json");
        defaultHeaders.put("Accept-Charset", "utf-8");
    }

    @Override
    public CompletableFuture<List<Annotation>> findAnnotations(UUID videoReferenceUuid) {
        return findAnnotations(videoReferenceUuid, null, null);
    }

    @Override
    public CompletableFuture<List<Annotation>> findAnnotations(UUID videoReferenceUuid, Long limit, Long offset) {
        return newResult(annoService.findByVideoReferenceUuid(videoReferenceUuid, limit, offset));
    }

    @Override
    public CompletableFuture<Long> countAnnotations(UUID videoReferenceUuid) {
        return newResult(annoService.countByVideoReferenceUuid(videoReferenceUuid));
    }

    @Override
    public CompletableFuture<Annotation> createAnnotation(Annotation annotation) {
        Long durationMillis = (annotation.getDuration() == null) ? null :
                annotation.getDuration().toMillis();
        Call<Annotation> call = annoService.create(annotation.getVideoReferenceUuid(),
                annotation.getConcept(),
                annotation.getObserver(),
                annotation.getObservationTimestamp(),
                annotation.getTimecode(),
                annotation.getRecordedTimestamp(),
                durationMillis,
                annotation.getGroup(),
                annotation.getActivity(),
                defaultHeaders);
        return newResult(call);
    }


    @Override
    public CompletableFuture<Association> createAssociation(UUID observationUuid, Association association) {
        Call<Association> call = assService.create(observationUuid,
                association.getLinkName(),
                association.getToConcept(),
                association.getLinkValue(),
                association.getMimeType(),
                defaultHeaders);
        return newResult(call);
    }

    @Override
    public CompletableFuture<Image> createImage(Image image) {
        String url = (image.getUrl() == null) ? null : image.getUrl().toExternalForm();
        String timecode = asString(image.getTimecode());
        Long elapsedTimeMillis = (image.getElapsedTime() == null) ? null : image.getElapsedTime().toMillis();

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
        return newResult(call);
    }

    @Override
    public CompletableFuture<Annotation> updateAnnotation(Annotation annotation) {
        Map<String, String> fieldMap = new HashMap<>();
        addField(fieldMap, "video_reference_uuid", annotation.getVideoReferenceUuid());
        addField(fieldMap, "concept", annotation.getConcept());
        addField(fieldMap, "observer", annotation.getObserver());
        addField(fieldMap, "timecode", annotation.getTimecode());
        Long elapsedTimeMillis = (annotation.getElapsedTime() == null) ? null : annotation.getElapsedTime().toMillis();
        addField(fieldMap, "elapse_time_millis", elapsedTimeMillis);
        addField(fieldMap, "recorded_timestamp", annotation.getRecordedTimestamp());
        Long durationMillis = (annotation.getDuration() == null) ? null : annotation.getDuration().toMillis();
        addField(fieldMap, "duration_millis", durationMillis);
        addField(fieldMap, "group", annotation.getGroup());
        addField(fieldMap, "activity", annotation.getActivity());
        return newResult(annoService.update(annotation.getObservationUuid(), fieldMap, defaultHeaders));
    }

    @Override
    public CompletableFuture<Association> updateAssociation(Association association) {
        Map<String, String> fieldMap = new HashMap<>();
        addField(fieldMap, "link_name", association.getLinkName());
        addField(fieldMap, "to_concept", association.getToConcept());
        addField(fieldMap, "link_value", association.getLinkValue());
        addField(fieldMap, "mime_type", association.getMimeType());
        return newResult(assService.update(association.getUuid(), fieldMap, defaultHeaders));
    }

    @Override
    public CompletableFuture<Image> updateImage(Image image) {
        Map<String, String> fieldMap = new HashMap<>();
        addField(fieldMap, "video_reference_uuid", image.getVideoReferenceUuid());
        String url = (image.getUrl() == null) ? null : image.getUrl().toExternalForm();
        addField(fieldMap, "url", url);
        addField(fieldMap, "timecode", image.getTimecode());
        Long elapsedTimeMillis = (image.getElapsedTime() == null) ? null : image.getElapsedTime().toMillis();
        addField(fieldMap, "elapsed_time_millis", elapsedTimeMillis);
        addField(fieldMap, "recorded_timestamp", image.getRecordedTimestamp());
        addField(fieldMap, "format", image.getFormat());
        addField(fieldMap, "width_pixels", image.getWidth());
        addField(fieldMap, "height_pixels", image.getHeight());
        addField(fieldMap, "description", image.getDescription());
        return newResult(imageService.update(image.getImageReferenceUuid(), fieldMap, defaultHeaders));
    }

    @Override
    public CompletableFuture<Boolean> deleteAnnotation(UUID observationUuid) {
        return newResult(annoService.delete(observationUuid, defaultHeaders));
    }

    @Override
    public CompletableFuture<Boolean> deleteAssociation(UUID associationUuid) {
        return newResult(assService.delete(associationUuid));
    }

    @Override
    public CompletableFuture<Boolean> deleteImage(UUID imageReferenceUuid) {
        return newResult(imageService.delete(imageReferenceUuid));
    }


    private static <T> CompletableFuture<T> newResult(Call<T> call) {
        CompletableFuture<T> f = new CompletableFuture<>();
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                f.complete(response.body());
            }

            @Override
            public void onFailure(Call<T> call, Throwable throwable) {
                f.completeExceptionally(throwable);
            }
        });
        return f;
    }

    private static String asString(Object obj) {
        return Optional.ofNullable(obj)
                .map(Object::toString)
                .orElseGet(null);
    }

    private static void addField(Map<String, String> map, String key, Object value) {
        if (value != null) {
            map.put(key, asString(value));
        }
    }



}
