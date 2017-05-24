package org.mbari.m3.vars.annotation.services.annosaurus.v1;

import org.mbari.m3.vars.annotation.model.Association;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.util.List;
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
}
