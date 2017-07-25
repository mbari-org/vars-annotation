package org.mbari.m3.vars.annotation.services.annosaurus.v1;

import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.AnnotationCount;
import org.mbari.vcr4j.time.Timecode;
import retrofit2.Call;
import retrofit2.http.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2017-05-22T16:17:00
 */
public interface AnnoWebService {


    @GET("annotations/{uuid}")
    Call<Annotation> findByUuid(@Path("uuid") UUID observationUuid);

    @GET("annotations/videoreference/{uuid}")
    Call<List<Annotation>> findByVideoReferenceUuid(@Path("uuid") UUID uuid,
                                                    @Query("limit") Long limit,
                                                    @Query("offset") Long offset);

    /**
     * This looks up the single imaged moment that the image is associated with, but
     * since this can contain several observations, it maps to a list of annotations.
     * @param uuid The image reference UUID
     * @return Annotations that are associated with this image
     */
    @GET("annotations/imagereference/{uuid}")
    Call<List<Annotation>> findByImageReferenceUuid(@Path("uuid") UUID uuid);

    /**
     *
     * @param uuid
     * @param concept
     * @param observer
     * @param observationTimestamp
     * @param timecode
     * @param recordedTimestamp
     * @param durationMillis
     * @param group
     * @param activity
     * @param headers Should include the token type: "Authorization":"BEARER [jwtstuff]"
     * @return
     */
    @FormUrlEncoded
    @POST("annotations")
    Call<Annotation> create(@Field("video_reference_uuid") UUID uuid,
                            @Field("concept") String concept,
                            @Field("observer") String observer,
                            @Field("observation_timestamp") Instant observationTimestamp,
                            @Field("timecode") Timecode timecode,
                            @Field("recorded_timestamp") Instant recordedTimestamp,
                            @Field("duration_millis") Long durationMillis,
                            @Field("group") String group,
                            @Field("activity") String activity,
                            @HeaderMap Map<String, String> headers);

    @POST("annotations/bulk")
    Call<Annotation> create(@Body List<Annotation> annotations );

    @FormUrlEncoded
    @PUT("annotations/{uuid}")
    Call<Annotation> update(@Path("uuid") UUID annotationUuid,
                            @FieldMap Map<String, String> fields,
                            @HeaderMap Map<String, String> headers);

    @PUT("annotations/bulk")
    Call<Annotation> update(@Body List<Annotation> annotations );

    @DELETE("observations/{uuid}")
    Call<Boolean> delete(@Path("uuid") UUID observationUuid,
                         @HeaderMap Map<String, String> headers);

    @POST("observations/delete")
    Call<Void> delete(@Body List<UUID> observationUuids);

    @GET("observations/videoreference/count/{uuid}")
    Call<AnnotationCount> countByVideoReferenceUuid(@Path("uuid") UUID videoReferenceUuid);

}
