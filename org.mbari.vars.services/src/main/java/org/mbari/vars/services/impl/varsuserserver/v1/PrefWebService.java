package org.mbari.vars.services.impl.varsuserserver.v1;

import org.mbari.vars.services.model.PreferenceNode;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;

/**
 * @author Brian Schlining
 * @since 2017-06-08T16:34:00
 */
public interface PrefWebService {


    @GET("prefs")
    Call<List<PreferenceNode>> findByName(@Query("name") String name);

    @GET("prefs")
    Call<PreferenceNode> findByNameAndKey(@Query("name") String name, @Query("key") String key);

    @GET("prefs/startswith/")
    Call<List<PreferenceNode>> findByNameLike(@Query("prefix") String name);

    @FormUrlEncoded
    @POST("prefs")
    Call<PreferenceNode> create(@Field("name") String name,
                                @Field("key") String key,
                                @Field("value") String value,
                                @HeaderMap Map<String, String> headers);

    @FormUrlEncoded
    @PUT("prefs")
    Call<PreferenceNode> update(@Query("name") String name,
                                @Query("key") String key,
                                @Field("value") String value,
                                @HeaderMap Map<String, String> headers);

    @DELETE("prefs")
    Call<Void> delete(@Query("name") String name,
                      @Query("key") String key);
}
