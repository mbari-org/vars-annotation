package org.mbari.vars.services.impl.annosaurus.v2;

import org.mbari.vars.services.model.Annotation;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2019-05-09T10:44:00
 */
public interface AnnoWebServiceV2 {

    @GET("annotations/videoreference/{uuid}")
    Call<List<Annotation>> findByVideoReferenceUuid(@Path("uuid") UUID uuid,
                                                    @Query("limit") Long limit,
                                                    @Query("offset") Long offset);

    @GET("annotations/videoreference/{uuid}")
    Call<List<Annotation>> findByVideoReferenceUuidAndTimestamps(@Path("uuid") UUID uuid,
                                                                 @Query("start") Instant start,
                                                                 @Query("end") Instant end,
                                                                 @Query("limit") Long limit,
                                                                 @Query("offset") Long offset);
}
