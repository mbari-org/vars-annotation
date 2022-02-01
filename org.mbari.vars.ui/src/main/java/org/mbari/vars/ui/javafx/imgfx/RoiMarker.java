package org.mbari.vars.ui.javafx.imgfx;

import javafx.scene.image.ImageView;
import org.mbari.imgfx.AutoscalePaneController;
import org.mbari.imgfx.roi.Localization;
import org.mbari.imgfx.roi.MarkerView;
import org.mbari.vars.services.model.Association;
import org.mbari.vars.ui.javafx.imgfx.domain.Json;
import org.mbari.vars.ui.javafx.imgfx.domain.Points;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RoiMarker implements Roi<MarkerView> {

    public static final String LINK_NAME = "localization-point";
    private static final Double DEFAULT_RADIUS = 10D;

    @Override
    public Optional<Localization<MarkerView, ImageView>> fromAssociation(String concept, Association association, AutoscalePaneController<ImageView> paneController) {
        var points = Json.GSON.fromJson(association.getLinkValue(), Points.class);
        return MarkerView.fromImageCoords(points.getX().get(0).doubleValue(),
                points.getY().get(0).doubleValue(),
                estimateRadius(paneController.getView()),
                paneController.getAutoscale())
            .map(dataView -> new Localization<>(dataView, paneController, association.getUuid(), concept));
    }

    @Override
    public Association fromLocalization(Localization<MarkerView, ImageView> localization, UUID imageReferenceUuid, String comment) {
        var marker = localization.getDataView().getData();
        var x = toInt(marker.getCenterX());
        var y = toInt(marker.getCenterY());
        var points = new Points(List.of(x), List.of(y), imageReferenceUuid, comment);
        var linkValue = Json.GSON.toJson(points);
        return new Association(LINK_NAME, Association.VALUE_SELF, linkValue, MEDIA_TYPE, localization.getUuid());
    }

    private double estimateRadius(ImageView imageView) {
        var image = imageView.getImage();
        if (image == null) {
            return DEFAULT_RADIUS;
        }
        else {
            return image.getWidth() / 60D;
        }
    }
}
