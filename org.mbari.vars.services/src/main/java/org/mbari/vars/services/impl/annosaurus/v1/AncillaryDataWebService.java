package org.mbari.m3.corelib.services.annosaurus.v1;

import org.mbari.m3.corelib.model.AncillaryData;
import org.mbari.m3.corelib.model.AncillaryDataDeleteCount;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2018-07-03T14:06:00
 */
public interface AncillaryDataWebService {

    /**
     * Create or updated.
     * @param data
     * @param headers
     * @return
     */
    @POST("ancillarydata/bulk")
    Call<List<AncillaryData>> createOrUpdate(@Body List<AncillaryData> data,
                                             @HeaderMap Map<String, String> headers);

    @GET("ancillarydata/videoreference/{uuid}")
    Call<List<AncillaryData>> findByVideoReferenceUuid(@Path("uuid") UUID videoReferenceUuid);

    @DELETE("ancillarydata/videoreference/{uuid}")
    Call<AncillaryDataDeleteCount> deleteByVideoReference(@Path("uuid") UUID videoReferenceUuid);


}
