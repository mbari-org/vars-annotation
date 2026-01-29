package org.mbari.vars.annotation.ui.javafx.mlstage;

import javafx.scene.image.ImageView;
import org.mbari.imgfx.AutoscalePaneController;
import org.mbari.imgfx.roi.Localization;
import org.mbari.imgfx.roi.RectangleView;
import org.mbari.vars.services.impl.annosaurus.v1.AnnoWebServiceFactory;
import org.mbari.vars.services.model.*;
import org.mbari.vcr4j.VideoIndex;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.mbari.vars.core.util.MathUtil.doubleToInt;

public class MLUtil {


    private MLUtil() {
        // No instantiation
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
            final var gson = AnnoWebServiceFactory.newGson();
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
