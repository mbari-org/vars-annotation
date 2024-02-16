package org.mbari.vars.services.impl.annosaurus.v1;

import org.mbari.vars.services.model.AnnotationCount;
import org.mbari.vars.services.model.ImagedMoment;
import org.mbari.vars.services.model.Index;
import retrofit2.Call;
import retrofit2.http.*;

import java.time.Instant;
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
    Call<Index> update(@Path("uuid") UUID imagedMomentUuid,
                       @FieldMap Map<String, String> fields,
                       @HeaderMap Map<String, String> headers);

    @GET("imagedmoments/videoreference/modified/{uuid}/{date}")
    Call<AnnotationCount> countByModifiedBefore(@Path("uuid") UUID videoReferenceUuid,
                                                @Path("date") Instant date);
}
