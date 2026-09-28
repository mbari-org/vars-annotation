package org.mbari.vars.annotation.test.services.ml;

import org.junit.jupiter.api.Test;
import org.mbari.vars.annotation.services.ml.MachineLearningResponseParser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MachineLearningResponseParserTest {

    @Test
    public void parsePredictEndpoint() {
        var json = """
                [{"concept":"Asteroidea","x":3.2,"y":88.6,"width":72.4,"height":46.3,"probability":0.59}]
                """;
        var xs = MachineLearningResponseParser.parse(json);
        assertEquals(1, xs.size());
        var x = xs.getFirst();
        assertEquals("Asteroidea", x.concept());
        assertEquals(0.59, x.confidence(), 0.0001);
        assertEquals(3, x.boundingBox().getX());
        assertEquals(89, x.boundingBox().getY());
        assertEquals(72, x.boundingBox().getWidth());
        assertEquals(46, x.boundingBox().getHeight());
    }

    @Test
    public void parsePredictorEndpoint() {
        var json = """
                {"success":true,"predictions":[{"category_id":"Asteroidea","scores":[0.59],
                 "bbox":[3.2,88.6,75.7,134.9]}]}
                """;
        var xs = MachineLearningResponseParser.parse(json);
        assertEquals(1, xs.size());
        var x = xs.getFirst();
        assertEquals("Asteroidea", x.concept());
        assertEquals(0.59, x.confidence(), 0.0001);
        assertEquals(3, x.boundingBox().getX());
        assertEquals(89, x.boundingBox().getY());
        assertEquals(73, x.boundingBox().getWidth());
        assertEquals(46, x.boundingBox().getHeight());
    }

    @Test
    public void parseEmpty() {
        assertTrue(MachineLearningResponseParser.parse("[]").isEmpty());
        assertTrue(MachineLearningResponseParser.parse("{\"success\":true,\"predictions\":[]}").isEmpty());
    }

    @Test
    public void parseGarbage() {
        assertThrows(IllegalArgumentException.class, () -> MachineLearningResponseParser.parse("\"nope\""));
    }
}
