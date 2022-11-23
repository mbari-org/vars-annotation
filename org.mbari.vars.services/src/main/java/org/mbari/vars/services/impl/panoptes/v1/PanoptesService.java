package org.mbari.vars.services.impl.panoptes.v1;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.mbari.vars.services.model.ImageUploadResults;
import org.mbari.vars.services.AuthService;
import org.mbari.vars.services.ImageArchiveService;
import org.mbari.vars.services.RetrofitWebService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Brian Schlining
 * @since 2017-08-31T14:03:00
 */
public class PanoptesService implements ImageArchiveService, RetrofitWebService {

    private final PanoptesWebService webService;
    private final Map<String, String> defaultHeaders;

    @Inject
    public PanoptesService(PanoptesWebServiceFactory serviceFactory,
                           @Named("PANOPTES_AUTH") AuthService authService) {
        webService = serviceFactory.create(PanoptesWebService.class, authService);
        defaultHeaders = new HashMap<>();
        defaultHeaders.put("Accept", "application/json");
        defaultHeaders.put("Accept-Charset", "utf-8");
    }

    @Override
    public CompletableFuture<ImageUploadResults> upload(String cameraId,
                                                        String deploymentId,
                                                        String name,
                                                        java.nio.file.Path image) {
        RequestBody requestBody = RequestBody.create(MultipartBody.FORM, image.toFile());
        MultipartBody.Part body = MultipartBody.Part.createFormData("file",
                image.getFileName().toString(),
                requestBody);
        return sendRequest(webService.uploadImage(cameraId, deploymentId, name, body,
                requestBody, defaultHeaders));
    }

    public CompletableFuture<ImageUploadResults> upload(String cameraId,
                                                        String deploymentId,
                                                        String name,
                                                        byte[] imageByes) {

        var mediaType = name.toLowerCase().endsWith("png") ?
                MediaType.parse("image/png") :
                MediaType.parse("image/jpeg");
        var requestBody = RequestBody.create(mediaType, imageByes);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file",
                name,
                requestBody);
        return sendRequest(webService.uploadImage(cameraId, deploymentId, name, body, requestBody, defaultHeaders));
    }


    @Override
    public CompletableFuture<ImageUploadResults> locate(String cameraId, String deploymentId, String name) {
        return sendRequest(webService.findImage(cameraId, deploymentId, name));
    }
}
