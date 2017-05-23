package org.mbari.m3.vars.annotation.services.annosaurus.v1;

import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Authorization;
import org.mbari.vcr4j.time.Timecode;
import retrofit2.Call;
import retrofit2.http.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2017-05-22T16:17:00
 */
public interface AnnotationWebService {


    @GET("annotations/{uuid}")
    Call<Annotation> findByUuid(@Path("uuid") UUID annotationUuid);

    @GET("annotations/videoreference/{uuid}")
    Call<List<Annotation>> findByVideoReferenceUuid(@Path("uuid") UUID uuid,
                                                    @Query("limit") long limit,
                                                    @Query("offset") long offset);


    /**
     *
     * @param uuid
     * @param concept
     * @param observer
     * @param observationTimestamp
     * @param timecode
     * @param recordedTimestamp
     * @param duration
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
                            @Field("duration_millis") Duration duration,
                            @Field("group") String group,
                            @Field("activity") String activity,
                            @HeaderMap Map<String, String> headers);

    @FormUrlEncoded
    @PUT("annotations/{uuid}")
    Call<Annotation> update(@Path("uuid") UUID annotationUuid,
                            @FieldMap Map<String, String> fields,
                            @HeaderMap Map<String, String> headers);

    @DELETE("annotations/{uuid}")
    Call<Boolean> delete(@Path("uuid") UUID annotationUuid,
                         @HeaderMap Map<String, String> headers);

    @He
    @GET("observations/videoreference/count/{uuid}")
    Call<Long> countByVideoReferenceUuid(@Path("uuid") UUID videoReferenceUuid);


}
