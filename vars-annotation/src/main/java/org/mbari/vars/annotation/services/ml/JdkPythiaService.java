package org.mbari.vars.annotation.services.ml;

import com.github.mizosoft.methanol.*;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.mbari.vars.annotation.services.MachineLearningService;
import org.mbari.vars.annotation.etc.methanol.LoggingInterceptor;
import org.mbari.vars.annotation.model.MachineLearningLocalization;
import org.mbari.vars.annotation.etc.jdk.Loggers;


import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Implementation for https://github.com/mbari-org/keras-model-server-fast-api
 * Test with httpie:
 *
 * <pre>
 *     <code>
 * http -f POST http://prometheus.shore.mbari.org:8082/predictor/ \
 *   model_type=image_queue_yolov5 \
 *   file@'/Users/brian/Downloads/testimages/20220302T214640Z--77cccf9c-691b-4eb4-bd74-49c290889dae.jpeg'
 *     </code>
 * </pre>
 *
 *
 * Original: This is having an issue where it is only working when the Charles proxy is running.
 * Then it works find. Otherwise it just seems to hang.  Use the OkHttp implementation instead.
 *
 * 2024-03-18: This is now working. I switched to a virtual thread when calling it..
 */
public class JdkPythiaService implements MachineLearningService {

    // Example: http://prometheus.shore.mbari.org:8082/predictor/
    private final String endpoint;
    private final Methanol client;
    private final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();
    private static final Loggers log = new Loggers(JdkPythiaService.class);

    public JdkPythiaService(String endpoint) {
        this.endpoint = endpoint;
        this.client = Methanol
                .newBuilder()
                // HTTP/1.1 is required because uvicorn/starlette does not correctly
                // handle multipart form data uploads over HTTP/2
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .interceptor(new LoggingInterceptor())
                .build();
    }

    @Override
    public List<MachineLearningLocalization> predict(Path image) {
        try {
            var bytes = java.nio.file.Files.readAllBytes(image);
            return predict(bytes);
        }
        catch (Exception e) {
            log.atWarn()
                    .withCause(e)
                    .log("Failed to run prediction on " + image );
        }
        return null;
    }

    @Override
    public List<MachineLearningLocalization> predict(byte[] jpegBytes) {

        try {
            log.atDebug().log("The JPEG image being sent for prediction is " + jpegBytes.length + " bytes long");

            var imagePart = MoreBodyPublishers.ofMediaType(
                    HttpRequest.BodyPublishers.ofByteArray(jpegBytes), MediaType.IMAGE_JPEG);
            var multipartBody = MultipartBodyPublisher.newBuilder()
                    .textPart("model_type", "image_queue_yolov5", StandardCharsets.UTF_8)
                    .formPart(
                            "file", UUID.randomUUID() + ".jpg", MoreBodyPublishers.ofMediaType(imagePart, MediaType.IMAGE_JPEG))
                    .build();
            var request = MutableRequest.POST(endpoint, multipartBody)
                    .header("Content-Type", multipartBody.mediaType().toString())
                    .header("Accept", "application/json");

            return sendRequest(request);
        }
        catch (Exception e) {
            log.atWarn()
                    .withCause(e)
                    .log("Failed to run prediction on image");
        }
        return null;
    }

    private List<MachineLearningLocalization> sendRequest(HttpRequest request) throws IOException, InterruptedException {
        var response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        log.atDebug().log(response);
        var prediction = gson.fromJson(response, MachineLearningResponse1.class);
        return prediction.toMLStandard();
    }
}
