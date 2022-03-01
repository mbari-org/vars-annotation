package org.mbari.vars.ui.javafx.imgfx;

import javafx.beans.property.ObjectProperty;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import org.mbari.imgfx.AutoscalePaneController;
import org.mbari.imgfx.roi.LineView;
import org.mbari.imgfx.roi.Localization;
import org.mbari.vars.services.model.Association;
import org.mbari.vars.ui.javafx.imgfx.domain.Json;
import org.mbari.vars.ui.javafx.imgfx.domain.Points;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RoiLine implements Roi<LineView> {

    public static String LINK_NAME = "localization-line";

    @Override
    public Optional<Localization<LineView, ImageView>> fromAssociation(String concept,
             Association association,
             AutoscalePaneController<ImageView> paneController,
             ObjectProperty<Color> editedColor) {
        var points = Json.GSON.fromJson(association.getLinkValue(), Points.class);
        return LineView.fromImageCoords(points.getX().get(0),
                points.getY().get(0),
                points.getX().get(1),
                points.getY().get(1),
                paneController.getAutoscale())
                .map(dataview -> new Localization<>(dataview, paneController, association.getUuid(), concept));
    }

    @Override
    public Association fromLocalization(Localization<LineView, ImageView> localization, UUID imageReferenceUuid, String comment) {
        var line = localization.getDataView().getData();
        var x0 = toInt(line.getStartX());
        var y0 = toInt(line.getStartY());
        var x1 = toInt(line.getEndX());
        var y1 = toInt(line.getEndY());
        var points = new Points(List.of(x0, x1), List.of(y0, y1), imageReferenceUuid, comment);
        var linkValue = Json.GSON.toJson(points);
        return new Association(LINK_NAME, Association.VALUE_SELF, linkValue, MEDIA_TYPE, localization.getUuid());
    }
}
