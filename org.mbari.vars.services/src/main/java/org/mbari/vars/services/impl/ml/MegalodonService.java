package org.mbari.vars.services.impl.ml;

import com.github.mizosoft.methanol.*;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.mbari.vars.core.util.StringUtils;
import org.mbari.vars.services.MachineLearningService;
import org.mbari.vars.services.model.MachineLearningLocalization;
import org.mbari.vars.services.util.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * Implementation for https://github.com/mbari-org/keras-model-server-fast-api
 */
public class MegalodonService implements MachineLearningService {

    // Example: http://prometheus.shore.mbari.org:8082/predictor/
    private final String endpoint;
    private final Methanol client;
    private final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();
    private static final Logger log = LoggerFactory.getLogger(MegalodonService.class);

    public MegalodonService(String endpoint) {
        this.endpoint = endpoint;
        this.client = Methanol.create();
    }

    @Override
    public List<MachineLearningLocalization> predict(Path image) {
        var mediaType = image.endsWith(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;

        try {
            var multipartBody = MultipartBodyPublisher.newBuilder()
                    .textPart("model_type", "image_queue_yolov5", StandardCharsets.UTF_8)
                    .filePart("file", image, mediaType)
                    .build();
            var request = MutableRequest.POST(endpoint, multipartBody)
                    .header("Accept", "application/json");

            return sendRequest(request);

        }
        catch (Exception e) {
            log.atWarn()
                    .setCause(e)
                    .log("Failed to run prediction on " + image );
        }
        return null;
    }

    @Override
    public List<MachineLearningLocalization> predict(BufferedImage image) {

        try {
            var imageBytes = ImageUtils.toJpegByteArray(image);

            var imagePart = MoreBodyPublishers.ofMediaType(
                    HttpRequest.BodyPublishers.ofByteArray(imageBytes), MediaType.IMAGE_JPEG);
            var multipartBody = MultipartBodyPublisher.newBuilder()
                    .textPart("model_type", "image_queue_yolov5", StandardCharsets.UTF_8)
                    .formPart(
                            "file", UUID.randomUUID() + ".jpg", MoreBodyPublishers.ofMediaType(imagePart, MediaType.IMAGE_JPEG))
                    .build();
            var request = MutableRequest.POST(endpoint, multipartBody)
                    .header("Accept", "application/json");

            return sendRequest(request);
        }
        catch (Exception e) {
            log.atWarn()
                    .setCause(e)
                    .log("Failed to run prediction on " + image.getWidth() + " x " + image.getHeight()  +" image");
        }
        return null;
    }

    private List<MachineLearningLocalization> sendRequest(HttpRequest request) throws IOException, InterruptedException {
        var response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        log.atInfo().log(response);
        var prediction = gson.fromJson(response, MachineLearningResponse1.class);
        return prediction.toMLStandard();
    }
}
