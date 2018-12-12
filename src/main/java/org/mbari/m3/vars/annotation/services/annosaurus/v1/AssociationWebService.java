package org.mbari.m3.vars.annotation.services.annosaurus.v1;

import org.mbari.m3.vars.annotation.model.Association;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2017-05-23T16:08:00
 */
public interface AssociationWebService {

    @GET("associations/{uuid}")
    Call<Association> findByUuid(@Path("uuid") UUID associationUuid);

    @GET("associations/{uuid}/{linkName}")
    Call<List<Association>> findByVideoReferenceAndLinkName(@Path("uuid") UUID videoReferenceUuid,
                                                            @Path("linkName") String linkName);

    @GET("associations/{uuid}/{linkName}")
    Call<List<Association>> findByVideoReferenceAndLinkNameAndConcept(@Path("uuid") UUID videoReferenceUuid,
                                                            @Path("linkName") String linkName,
                                                            @Query("concept") String concept);

    @FormUrlEncoded
    @POST("associations")
    Call<Association> create(@Field("observation_uuid") UUID observationUuid,
                             @Field("link_name") String linkName,
                             @Field("to_concept") String toConcept,
                             @Field("link_value") String linkValue,
                             @Field("mime_type") String mimeType,
                             @HeaderMap Map<String, String> headers);

    @FormUrlEncoded
    @PUT("associations/{uuid}")
    Call<Association> update(@Path("uuid") UUID associationUuid,
                             @FieldMap Map<String, String> fields,
                             @HeaderMap Map<String, String> headers);

    @PUT("associations/bulk")
    Call<Collection<Association>> update(@Body Collection<Association> associations);

    @DELETE("associations/{uuid}")
    Call<Boolean> delete(@Path("uuid") UUID associationUuid);

    @POST("associations/delete")
    Call<Void> delete(@Body Collection<UUID> associationUuids,
                      @HeaderMap Map<String, String> headers);

}
