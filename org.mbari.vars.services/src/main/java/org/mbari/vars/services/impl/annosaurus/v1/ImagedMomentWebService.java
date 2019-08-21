package org.mbari.m3.corelib.services.annosaurus.v1;

import org.mbari.m3.corelib.model.ImagedMoment;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2017-11-21T15:43:00
 */
public interface ImagedMomentWebService {

    @GET("imagedmoments/videoreference/{uuid}")
    Call<List<ImagedMoment>> findByVideoReferenceUuid(@Path("uuid") UUID videoReferenceUuid);

    @FormUrlEncoded
    @PUT("imagedmoments/{uuid}")
    Call<ImagedMoment> update(@Path("uuid") UUID imagedMomentUuid,
            @FieldMap Map<String, String> fields,
            @HeaderMap Map<String, String> headers);
}
