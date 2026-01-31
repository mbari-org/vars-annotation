package org.mbari.vars.annotation.services.ml;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.mbari.vars.annotation.services.MachineLearningService;
import org.mbari.vars.annotation.model.MachineLearningLocalization;
import org.mbari.vars.annotation.util.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class OkHttpMegalodonService  implements MachineLearningService {

    private final String endpoint;
    private final OkHttpClient client;
    private final MediaType jpegType = MediaType.parse("image/jpg");
    private static final Logger log = LoggerFactory.getLogger(OkHttpMegalodonService.class);
    private static final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    public OkHttpMegalodonService(String endpoint) {
        this(endpoint, Duration.ofSeconds(30));
    }

    public OkHttpMegalodonService(String endpoint, Duration timeout) {
        this.endpoint = endpoint;
        client = new OkHttpClient()
                .newBuilder()
                .readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .writeTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .addInterceptor(new HttpLoggingInterceptor())
                .build();
    }

    @Override
    public List<MachineLearningLocalization> predict(Path image) {
        try {
            final var bufferedImage = ImageIO.read(image.toFile());
            final var jpegBytes = ImageUtils.toJpegByteArray(bufferedImage);
            return predict(jpegBytes);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<MachineLearningLocalization> predict(byte[] jpegBytes) {
        var requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("model_type", "image_queue_yolov5")
                .addFormDataPart("file", UUID.randomUUID() + ".jpg",
                        RequestBody.create(jpegType, jpegBytes))
                .build();

        var request = new Request.Builder()
                .header("Accept", "application/json")
                .url(endpoint)
                .post(requestBody)
                .build();

        try (var response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            var body = response.body().string();
            log.atDebug().log("Response from ml service: " + body);
            var prediction = gson.fromJson(body, MachineLearningResponse1.class);
            return prediction.toMLStandard();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
