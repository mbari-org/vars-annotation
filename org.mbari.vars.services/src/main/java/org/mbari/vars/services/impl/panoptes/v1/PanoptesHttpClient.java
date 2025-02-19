package org.mbari.vars.services.impl.panoptes.v1;

import com.github.mizosoft.methanol.MoreBodyPublishers;
import com.github.mizosoft.methanol.MultipartBodyPublisher;
import com.github.mizosoft.methanol.MutableRequest;
import com.google.gson.Gson;
import okhttp3.MediaType;
import org.mbari.vars.core.util.UriUtils;
import org.mbari.vars.services.ImageArchiveService;
import org.mbari.vars.services.impl.BaseHttpClient;
import org.mbari.vars.services.impl.JwtHttpClient;
import org.mbari.vars.services.impl.annosaurus.v1.AnnoWebServiceFactory;
import org.mbari.vars.services.impl.annosaurus.v1.AnnosaurusHttpClient;
import org.mbari.vars.services.model.Authorization;
import org.mbari.vars.services.model.ImageUploadResults;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class PanoptesHttpClient extends BaseHttpClient implements ImageArchiveService {

    private final JwtHttpClient jwtHttpClient;

    private final Gson gson = PanoptesWebServiceFactory.newGson();
    private final Gson snakeCaseGson = AnnoWebServiceFactory.newGson();

    public PanoptesHttpClient(HttpClient client, URI baseUri, String apiKey) {
        super(client, baseUri);
        this.jwtHttpClient = new JwtHttpClient(client,
                buildUri("/auth"),
                "Authorization", "APIKEY " + apiKey,
                body -> snakeCaseGson.fromJson(body, Authorization.class));
    }

    public PanoptesHttpClient(String baseUri, Duration timeout, String apiKey) {
        this(newHttpClient(timeout), URI.create(baseUri), apiKey);
    }

    @Override
    public CompletableFuture<ImageUploadResults> locate(String cameraId, String deploymentId, String filename) {
        var uri = buildUri(cameraId, deploymentId, filename);
        var request = MutableRequest.GET(uri)
                .header("Accept", "application/json")
                .build();
        return submit(request, 200, body -> gson.fromJson(body, ImageUploadResults.class));
    }


    @Override
    public CompletableFuture<ImageUploadResults> upload(String cameraId, String deploymentId, String filename, Path image) {
        var auth = jwtHttpClient.authorizeIfNeeded();
        var uri = buildUri(cameraId, deploymentId, filename);
        log.atInfo().log("Uploading image to " + uri);
        try {
            var multipartBody = MultipartBodyPublisher.newBuilder()
                    .filePart("file", image)
                    .build();
            var request = MutableRequest.POST(uri, multipartBody)
                    .header("Authorization", "Bearer " + auth.getAccessToken())
                    .build();
            return submit(request, 200, body -> gson.fromJson(body, ImageUploadResults.class));

        }
        catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<ImageUploadResults> upload(String cameraId, String deploymentId, String filename, byte[] imageByes) {
        var auth = jwtHttpClient.authorizeIfNeeded();
        log.atInfo().log("Authorized! " + auth);
        var uri = buildUri(cameraId, deploymentId, filename);
        log.atInfo().log("Uploading image bytes to " + uri);
        var mediaType = filename.toLowerCase().endsWith("png") ? "image/png" : "image/jpeg";
        var methanolMediaType = com.github.mizosoft.methanol.MediaType.parse(mediaType);
        try {
            var bodyPublisher = MoreBodyPublishers.ofMediaType(HttpRequest.BodyPublishers.ofByteArray(imageByes), methanolMediaType);
            var multipartBody = MultipartBodyPublisher.newBuilder()
                    .formPart("file", filename, bodyPublisher)
                    .build();
            var request = MutableRequest.POST(uri, multipartBody)
                    .header("Authorization", "Bearer " + auth.getAccessToken())
                    .build();
            return submit(request, 200, body -> gson.fromJson(body, ImageUploadResults.class));

        }
        catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private URI buildUri(String cameraId, String deploymentId, String filename){
        var encodedCamera = UriUtils.encodeURIComponent(cameraId);
        var encodedDeployment = UriUtils.encodeURIComponent(deploymentId);
        var encodedFilename = UriUtils.encodeURIComponent(filename);
        return buildUri("/images/" + encodedCamera + "/" + encodedDeployment + "/" + encodedFilename);
    }
}
