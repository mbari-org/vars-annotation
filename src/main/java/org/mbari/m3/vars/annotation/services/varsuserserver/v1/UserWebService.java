package org.mbari.m3.vars.annotation.services.varsuserserver.v1;

import org.mbari.m3.vars.annotation.model.User;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;

/**
 * @author Brian Schlining
 * @since 2017-06-08T16:18:00
 */
public interface UserWebService {

    @GET("users")
    Call<List<User>> findAll();

    @FormUrlEncoded
    @POST("users")
    Call<User> create(@Field("username") String username,
                      @Field("password") String password,
                      @Field("role") String role,
                      @Field("first_name") String firstName,
                      @Field("last_name") String lastName,
                      @Field("affiliation") String affiliation,
                      @Field("email") String email,
                      @HeaderMap Map<String, String> headers);

    @PUT("users/{name}")
    Call<User> update(@Path("name") String username,
                      @FieldMap Map<String, String> fields,
                      @HeaderMap Map<String, String> headers);


}
