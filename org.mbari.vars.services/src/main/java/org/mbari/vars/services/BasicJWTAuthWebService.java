package org.mbari.vars.services;

import org.mbari.vars.services.model.Authorization;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * @author Brian Schlining
 * @since 2017-05-23T15:57:00
 */
public interface BasicJWTAuthWebService {

    /**
     *
     * @param auth This needs to include the authorization type. e.g.
     *                     "APIKEY [clientSecret]"
     * @return
     */
    @POST("auth")
    Call<Authorization> authorize(@Header("Authorization") String auth);

}
