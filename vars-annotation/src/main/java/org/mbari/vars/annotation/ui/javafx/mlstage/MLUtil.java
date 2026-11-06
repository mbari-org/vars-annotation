package org.mbari.vars.annotation.ui.javafx.mlstage;

import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import org.mbari.imgfx.AutoscalePaneController;
import org.mbari.imgfx.roi.Localization;
import org.mbari.imgfx.roi.RectangleView;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annosaurus.sdk.r1.models.BoundingBox;
import org.mbari.vars.annotation.etc.gson.Gsons;
import org.mbari.vars.annosaurus.sdk.r1.models.Association;
import org.mbari.vars.annotation.model.MachineLearningLocalization;
import org.mbari.vars.annotation.ui.util.ColorUtil;
import org.mbari.vcr4j.VideoIndex;
import static org.mbari.vars.annotation.util.MathUtil.doubleToInt;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MLUtil {


    /** Opacity of a localization's fill. Low enough that the underlying image shows through. */
    public static final double FILL_OPACITY = 0.25;

    /** Used when a localization has no concept */
    private static final String DEFAULT_COLOR = "#FF9800";

    private MLUtil() {
        // No instantiation
    }

    /**
     * @param concept The concept name of a localization
     * @return The opaque color for the concept. The same concept always gets the same color.
     */
    public static Color colorFor(String concept) {
        if (concept == null || concept.isBlank()) {
            return Color.web(DEFAULT_COLOR);
        }
        return Color.web(ColorUtil.stringToHexColor(concept, 1.0));
    }

    /**
     * Some concepts hash to dark colors that are unreadable as text on a dark background.
     * This blends the color toward white (keeping its hue) until it reaches the WCAG AA
     * contrast ratio for text (4.5:1) against the background, or until a maximum number of blend steps is reached.
     */
    public static Color readableOn(Color color, Color background) {
        var readable = color;
        for (int i = 0; i < 10 && contrastRatio(readable, background) < 4.5; i++) {
            readable = readable.interpolate(Color.WHITE, 0.15);
        }
        return readable;
    }

    private static double contrastRatio(Color a, Color b) {
        var la = relativeLuminance(a);
        var lb = relativeLuminance(b);
        return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
    }

    private static double relativeLuminance(Color c) {
        return 0.2126 * linearize(c.getRed()) + 0.7152 * linearize(c.getGreen()) + 0.0722 * linearize(c.getBlue());
    }

    private static double linearize(double v) {
        return v <= 0.03928 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
    }

    /**
     * Colors a localization's box from its concept: a translucent fill so the image
     * shows through, plus a solid outline so the box is still easy to see.
     */
    public static void applyConceptColor(Localization<RectangleView, ImageView> localization) {
        var concept = localization.getLabel();
        var color = colorFor(concept);
        var fill = (concept == null || concept.isBlank())
                ? Color.web(DEFAULT_COLOR, FILL_OPACITY)
                : Color.web(ColorUtil.stringToHexColor(concept, FILL_OPACITY));
        var rectangle = localization.getDataView().getView();
        rectangle.setFill(fill);
        rectangle.setStroke(color);
        rectangle.setStrokeWidth(2);
    }

    public static Optional<Localization<RectangleView, ImageView>> toLocalization(MachineLearningLocalization ml,
                                                                                  AutoscalePaneController<ImageView> autoscale) {
        var b = ml.boundingBox();
        var view = RectangleView.fromImageCoords((double) b.getX(),
                (double) b.getY(),
                (double) b.getWidth(),
                (double) b.getHeight(),
                autoscale.getAutoscale());
        return view.map(v -> new Localization<>(v, autoscale, ml.concept()));
    }

    public static Optional<Annotation> toAnnotation(String observer,
                                                    String group,
                                                    String activity,
                                                    VideoIndex videoIndex,
                                                    UUID videoReferenceUuid,
                                                    Localization<RectangleView, ImageView> localization) {

        if (localization.isVisible()) {
            // Build association that defines bounding box
            final var data = localization.getDataView().getData();
            final var x = doubleToInt(data.getX());
            final var y = doubleToInt(data.getY());
            final var width = doubleToInt(data.getWidth());
            final var height = doubleToInt(data.getHeight());
            final var boundingBox = new BoundingBox(x, y, width, height, "VARS Annotation");
            final var gson = Gsons.SNAKE_CASE_GSON;
            final var json = gson.toJson(boundingBox);
            final var association = new Association(BoundingBox.LINK_NAME, Association.VALUE_SELF, json, "application/json");

            // Build annotation
            final var concept = localization.labelProperty().get();
            final var annotation = new Annotation(concept, observer, videoIndex, videoReferenceUuid);
            annotation.setAssociations(List.of(association));
            annotation.setGroup(group);
            annotation.setActivity(activity);
            return Optional.of(annotation);
        }
        return Optional.empty();

    }

}
