package org.mbari.vars.annotation.services.ml;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import org.mbari.vars.annosaurus.sdk.r1.models.BoundingBox;
import org.mbari.vars.annotation.model.MachineLearningLocalization;

import java.util.List;

/**
 * Parses the responses of the Pythia service. Pythia has two endpoints that return the same
 * localizations in different forms. Staff sometimes configure the wrong one, so we accept both:
 * <ul>
 *     <li><code>/predict</code> returns a JSON array of
 *     <code>{"concept", "x", "y", "width", "height", "probability"}</code></li>
 *     <li><code>/predictor</code> (keras-model-server format) returns an object,
 *     <code>{"success", "predictions": [{"category_id", "scores", "bbox"}]}</code>
 *     (see {@link MachineLearningResponse1})</li>
 * </ul>
 */
public class MachineLearningResponseParser {

    private static final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private MachineLearningResponseParser() {
        // no instantiation
    }

    /** Bounding box as returned by the <code>/predict</code> endpoint. */
    private record PredictBox(String concept, double x, double y, double width, double height, double probability) {}

    public static List<MachineLearningLocalization> parse(String json) {
        var element = JsonParser.parseString(json);
        if (element.isJsonArray()) {
            return java.util.Arrays.stream(gson.fromJson(element, PredictBox[].class))
                    .map(MachineLearningResponseParser::toLocalization)
                    .toList();
        }
        else if (element.isJsonObject()) {
            var response = gson.fromJson(element, MachineLearningResponse1.class);
            return response.getPredictions() == null ? List.of() : response.toMLStandard();
        }
        throw new IllegalArgumentException("Unrecognized response from ML service: " + json);
    }

    private static MachineLearningLocalization toLocalization(PredictBox p) {
        var box = new BoundingBox(round(p.x()), round(p.y()), round(p.width()), round(p.height()));
        return new MachineLearningLocalization(p.concept(), p.probability(), box);
    }

    private static int round(double v) {
        return Math.toIntExact(Math.round(v));
    }
}
