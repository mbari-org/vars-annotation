package org.mbari.vars.services.impl.panoptes.v1;

import okhttp3.MultipartBody;
import org.mbari.vars.services.model.ImageUploadResults;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

/**
 * @author Brian Schlining
 * @since 2017-08-31T13:07:00
 */
public interface PanoptesWebService {

    @Multipart
    @POST("images/{camera_id}/{deployment_id}/{filename}")
    Call<ImageUploadResults> uploadImage(@Path("camera_id") String cameraId,
                                         @Path("deployment_id") String deploymentId,
                                         @Path("filename") String filename,
                                         @Part MultipartBody.Part image,
                                         @HeaderMap Map<String, String> headers);

    @GET("images/{camera_id}/{deployment_id}/{filename}")
    Call<ImageUploadResults> findImage(@Path("camera_id") String cameraId,
                                       @Path("deployment_id") String deploymentId,
                                       @Path("filename") String filename);
}
