package org.mbari.m3.vars.annotation.services.annosaurus.v1;

import org.mbari.m3.vars.annotation.model.Image;
import org.mbari.m3.vars.annotation.model.ImageReference;
import retrofit2.Call;
import retrofit2.http.*;

import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2017-05-23T16:08:00
 */
public interface ImageWebService {

    @GET("images/{uuid}")
    Call<Image> findByUuid(@Path("uuid") UUID imageReferenceUuid);

    @GET("images/videoreference/{uuid}")
    Call<List<Image>> findByVideoReferenceUuid(@Path("uuid") UUID videoReferenceUuid);

    @GET("images/url/{url}")
    Call<Image> findByUrl(@Path("url") String url);

    @FormUrlEncoded
    @POST("images")
    Call<Image> create(@Field("video_reference_uuid") UUID videoReferenceUuid,
                       @Field("url") String url,
                       @Field("timecode") String timecode,
                       @Field("elapsed_time_millis") Long elaspedTimeMillis,
                       @Field("recorded_timestamp") Instant recordedTimestamp,
                       @Field("format") String format,
                       @Field("width_pixels") Integer width,
                       @Field("height_pixels") Integer height,
                       @Field("description") String description,
                       @HeaderMap Map<String, String> headers);

    @FormUrlEncoded
    @PUT("images/{uuid}")
    Call<Image> update(@Path("uuid") UUID imageReferenceUuid,
                       @FieldMap Map<String, String> fields,
                       @HeaderMap Map<String, String> headers);

    @DELETE("imagereferences/{uuid}")
    Call<Boolean> delete(@Path("uuid") UUID imageReferenceUuid);

}
