package org.mbari.vars.services.impl.ml;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * This maps the output of https://github.com/mbari-org/keras-model-server-fast-api to Java.
 * Example json:
 *
 * <pre><code>
 *  {
 *   "success": true,
 *   "predictions": [
 *     {
 *       "category_id": "Microstomus pacificus",
 *       "scores": [
 *         0.874488115310669
 *       ],
 *       "bbox": [
 *         419.2205810546875,
 *         329.05548095703125,
 *         527.6322021484375,
 *         484.7042541503906
 *       ]
 *     },
 *     {
 *       "category_id": "Asteroidea",
 *       "scores": [
 *         0.5905897617340088
 *       ],
 *       "bbox": [
 *         3.2457876205444336,
 *         88.5366439819336,
 *         75.68214416503906,
 *         134.9093475341797
 *       ]
 *     }
 *   ]
 * }
 * </code></pre>
 */
public class MachineLearningPrediction1 {

    @SerializedName("category_id")
    private String categoryId;

    private List<Double> scores;

    @SerializedName("bbox")
    private List<Double> coords;

    public MachineLearningPrediction1() {
    }

    public MachineLearningPrediction1(String categoryId, List<Double> scores, List<Double> coords) {
        this.categoryId = categoryId;
        this.scores = scores;
        this.coords = coords;
    }

    public Integer getX() {
        return Math.toIntExact(Math.round(Math.min(coords.get(0), coords.get(2))));
    }

    public Integer getY() {
        return Math.toIntExact(Math.round(Math.min(coords.get(1), coords.get(3))));
    }

    public Integer getWidth() {
        return Math.toIntExact(Math.round(Math.abs(coords.get(0) - coords.get(2))));
    }

    public Integer getHeight() {
        return Math.toIntExact(Math.round(Math.abs(coords.get(1) - coords.get(3))));
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public List<Double> getScores() {
        return scores;
    }

    public void setScores(List<Double> scores) {
        this.scores = scores;
    }

    public List<Double> getCoords() {
        return coords;
    }

    public void setCoords(List<Double> coords) {
        this.coords = coords;
    }

}
