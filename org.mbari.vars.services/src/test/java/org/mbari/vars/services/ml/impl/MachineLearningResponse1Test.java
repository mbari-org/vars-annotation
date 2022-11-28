package org.mbari.vars.services.ml.impl;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;
import static  org.junit.Assert.*;
import org.mbari.vars.services.impl.ml.MachineLearningResponse1;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

public class MachineLearningResponse1Test {

    private Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    @Test
    public void testToLocalization() throws Exception {
        var jsonUrl = getClass().getResource("/json/megalodon.json");
        var jsonPath = Paths.get(jsonUrl.toURI());
        var json = Files.readString(jsonPath);
        var prediction = gson.fromJson(json, MachineLearningResponse1.class);
        assertEquals(3, prediction.getPredictions().size());
        var ml = prediction.toMLStandard();
        assertEquals(3, ml.size());
    }
}
