package org.mbari.vars.annotation.ui.javafx.imgfx;

import javafx.beans.property.ObjectProperty;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import org.mbari.imgfx.AutoscalePaneController;
import org.mbari.imgfx.roi.Localization;
import org.mbari.imgfx.roi.RectangleView;
import org.mbari.imgfx.roi.RectangleViewEditor;
import org.mbari.vars.annosaurus.sdk.r1.models.Association;
import org.mbari.vars.annosaurus.sdk.r1.models.BoundingBox;
import org.mbari.vars.annotation.etc.gson.Gsons;
import org.mbari.vars.annotation.services.annosaurus.BoundingBoxes;
import org.mbari.vars.annotation.ui.javafx.imagestage.Json;

import java.util.Optional;
import java.util.UUID;

public class RoiBoundingBox implements Roi<RectangleView> {

    public static String LINK_NAME = "bounding box";

    @Override
    public Optional<Localization<RectangleView, ImageView>> fromAssociation(String concept,
                                                                            Association association,
                                                                            AutoscalePaneController<ImageView> paneController,
                                                                            ObjectProperty<Color> editedColor) {

        var boundingBox = Gsons.SNAKE_CASE_GSON.fromJson(association.getLinkValue(), BoundingBox.class);
        return RectangleView.fromImageCoords(
                        (double) boundingBox.getX(),
                        (double) boundingBox.getY(),
                        (double) boundingBox.getWidth(),
                        (double) boundingBox.getHeight(),
                        paneController.getAutoscale())
                .map(dataView -> {
                    // Rectangles get an editor added
                    var editor = new RectangleViewEditor(dataView, paneController.getPane());
                    editor.editColorProperty().bind(editedColor);
                    return dataView;
                })
                .map(dataView -> new Localization<>(dataView, paneController, association.getUuid(), concept));
    }

    public Optional<Localization<RectangleView, ImageView>> fromAssociation(String concept,
                                                                            Association association,
                                                                            AutoscalePaneController<ImageView> paneController) {
        var boundingBox = Json.GSON.fromJson(association.getLinkValue(), BoundingBox.class);
        return RectangleView.fromImageCoords(
                        (double) boundingBox.getX(),
                        (double) boundingBox.getY(),
                        (double) boundingBox.getWidth(),
                        (double) boundingBox.getHeight(),
                        paneController.getAutoscale())
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
        //TODO align with latest annosaurus-java-sdk constructor
        var boundingBox = new BoundingBox(x, y, width, height, BoundingBoxes.GENERATOR, imageReferenceUuid, comment);
        var linkValue = Json.GSON.toJson(boundingBox);
        return new Association(LINK_NAME, Association.VALUE_SELF, linkValue, MEDIA_TYPE, localization.getUuid());
    }


}
