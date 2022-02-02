package org.mbari.vars.ui.javafx.imgfx;

import javafx.beans.property.ObjectProperty;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import org.mbari.imgfx.AutoscalePaneController;
import org.mbari.imgfx.roi.Localization;
import org.mbari.imgfx.roi.RectangleView;
import org.mbari.imgfx.roi.RectangleViewEditor;
import org.mbari.vars.services.model.Association;
import org.mbari.vars.ui.javafx.imgfx.domain.BoundingBox;
import org.mbari.vars.ui.javafx.imgfx.domain.Json;

import java.util.Optional;
import java.util.UUID;

public class RoiBoundingBox implements Roi<RectangleView> {

    public static String LINK_NAME = "bounding box";

    @Override
    public Optional<Localization<RectangleView, ImageView>> fromAssociation(String concept,
                                                                            Association association,
                                                                            AutoscalePaneController<ImageView> paneController,
                                                                            ObjectProperty<Color> editedColor) {

        var boundingBox = Json.GSON.fromJson(association.getLinkValue(), BoundingBox.class);
        return RectangleView.fromImageCoords(
                        boundingBox.getX().doubleValue(),
                        boundingBox.getY().doubleValue(),
                        boundingBox.getWidth().doubleValue(),
                        boundingBox.getHeight().doubleValue(),
                        paneController.getAutoscale())
                .map(dataView -> {
                    // Rectangles get an editor added
                    var editor = new RectangleViewEditor(dataView, paneController.getPane());
                    editor.editColorProperty().bind(editedColor);
                    return dataView;
                })
                .map(dataView -> new Localization<>(dataView, paneController, association.getUuid(), concept));
    }

    @Override
    public Association fromLocalization(Localization<RectangleView, ImageView> localization,
                                        UUID imageReferenceUuid,
                                        String comment) {
        var rect = localization.getDataView().getData();
        var x = toInt(rect.getX());
        var y = toInt(rect.getY());
        var width = toInt(rect.getWidth());
        var height = toInt(rect.getHeight());
        var boundingBox = new BoundingBox(x, y, width, height, imageReferenceUuid, comment);
        var linkValue = Json.GSON.toJson(boundingBox);
        return new Association(LINK_NAME, Association.VALUE_SELF, linkValue, MEDIA_TYPE, localization.getUuid());
    }


}
