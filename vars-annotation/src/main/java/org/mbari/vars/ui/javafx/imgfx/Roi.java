package org.mbari.vars.ui.javafx.imgfx;

import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import org.mbari.imgfx.AutoscalePaneController;
import org.mbari.imgfx.roi.Data;
import org.mbari.imgfx.roi.DataView;
import org.mbari.imgfx.roi.Localization;
import org.mbari.vars.services.model.Association;

import java.util.Optional;
import java.util.UUID;

public interface Roi<C extends DataView<? extends Data, ? extends Shape>> {

    String MEDIA_TYPE = "application/json";

    Optional<Localization<C, ImageView>> fromAssociation(String concept,
                                                         Association association,
                                                         AutoscalePaneController<ImageView> paneController,
                                                         ObjectProperty<Color> editedColor);

    Association fromLocalization(Localization<C, ImageView> localization, UUID imageReferenceUuid, String comment);

    default int toInt(Double d) {
        return (int) Math.round(d);
    }
}
