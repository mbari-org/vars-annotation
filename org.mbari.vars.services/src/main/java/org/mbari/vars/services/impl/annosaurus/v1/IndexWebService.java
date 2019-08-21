package org.mbari.m3.corelib.services.annosaurus.v1;

import org.mbari.m3.corelib.model.Index;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2019-02-08T15:00:00
 */
public interface IndexWebService {

    @GET("index/videoreference/{uuid}")
    Call<List<Index>> findByVideoReferenceUuid(@Path("uuid") UUID videoReferenceUuid);

    @PUT("index/tapetime")
    Call<List<Index>> update(@Body Collection<Index> indices,
                                  @HeaderMap Map<String, String> headers);
}
