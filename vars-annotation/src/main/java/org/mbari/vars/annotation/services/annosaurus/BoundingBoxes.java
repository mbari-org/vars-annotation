package org.mbari.vars.annotation.services.annosaurus;

import javafx.scene.image.ImageView;
import org.mbari.imgfx.AutoscalePaneController;
import org.mbari.imgfx.roi.Localization;
import org.mbari.imgfx.roi.RectangleView;
import org.mbari.vars.annosaurus.sdk.r1.models.Association;
import org.mbari.vars.annosaurus.sdk.r1.models.BoundingBox;
import org.mbari.vars.annotation.etc.gson.Gsons;
import org.mbari.vars.annotation.ui.javafx.imagestage.Json;

import java.util.Optional;

public class BoundingBoxes {

    public static final String GENERATOR = "VARS Annotation";

    public static Optional<BoundingBox> fromAssociation(Association association) {
        try {
            var box = Gsons.SNAKE_CASE_GSON.fromJson(association.getLinkValue(), BoundingBox.class);
            return Optional.of(box);
        }
        catch (Exception e) {
            return Optional.empty();
        }
    }

    public static Optional<Localization<RectangleView, ImageView>> fromAssociation(String concept,
                                                                                   Association association,
                                                                                   AutoscalePaneController<ImageView> paneController) {
        var boundingBox = Json.GSON.fromJson(association.getLinkValue(), BoundingBox.class);
        return RectangleView.fromImageCoords((double) boundingBox.getX(),
                        (double) boundingBox.getY(),
                        (double) boundingBox.getWidth(),
                        (double) boundingBox.getHeight(),
                        paneController.getAutoscale())
                .map(dataView -> new Localization<>(dataView, paneController, association.getUuid(), concept));
    }
}
