package org.mbari.vars.services.impl.annosaurus.v1;

import org.mbari.vars.services.model.CachedVideoReference;
import org.mbari.vars.services.model.ValueList;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;
import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2019-11-12T10:54:00
 */
public interface VideoInfoWebService {

    @GET("videoreferences/missionids")
    Call<ValueList> findAllMissionIds();

    @GET("videoreferences/missioncontacts")
    Call<ValueList> findAllMissionContacts();

    @GET("videoreferences/{uuid}")
    Call<CachedVideoReference> findByUuid(@Path("uuid") UUID uuid);

    @GET("videoreferences/videoreferences")
    Call<ValueList> findAllVideoReferenceUuids();

    @GET("videoreferences/videoreference/{uuid}")
    Call<CachedVideoReference>  findByVideoReferenceUuid(@Path("uuid") UUID videoReferenceUuid);

    @FormUrlEncoded
    @POST("videoreferences")
    Call<CachedVideoReference> create(@Field("video_reference_uuid") UUID videoReferenceUuid,
                                      @Field("mission_id") String missionId,
                                      @Field("platform_name") String platformName,
                                      @Field("mission_contact") String missionContact,
                                      @HeaderMap Map<String, String> headers);

    @FormUrlEncoded
    @PUT("videoreferences/{uuid}")
    Call<CachedVideoReference> update(@Path("uuid") UUID uuid,
                                      @FieldMap Map<String, String> fields,
                                      @HeaderMap Map<String, String> headers);

    @DELETE("videoreferences/{uuid}")
    Call<Void> delete(@Path("uuid") UUID uuid, @HeaderMap Map<String, String> headers);

}
