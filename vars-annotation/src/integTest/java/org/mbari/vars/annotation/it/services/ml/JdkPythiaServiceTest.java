package org.mbari.vars.annotation.it.services.ml;

import com.typesafe.config.ConfigFactory;
import org.junit.jupiter.api.Test;
import org.mbari.vars.annotation.it.ITConfig;
import org.mbari.vars.annotation.services.ml.JdkPythiaService;

import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

public class JdkPythiaServiceTest {

    @Test
    public void testPredict() {
        var imageUrl = getClass().getResource("/images/MCC-00014.jpg");
        assertNotNull(imageUrl, "Image URL should not be null");
        Path imagePath = Path.of(imageUrl.getPath());
        assertTrue(imagePath.toFile().exists(), "Image file should exist");

        var endpoint = "http://perceptron.shore.mbari.org:8080/predictor";

        var service = new JdkPythiaService(endpoint);

        var result = service.predict(imagePath);
        assertNotNull(result, "Prediction result should not be null");
        assertFalse(result.isEmpty(), "Prediction result should not be empty");

    }
}
