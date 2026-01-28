package org.mbari.vars.ui.javafx.imgfx.events;

import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Shape;
import org.mbari.imgfx.etc.rx.events.*;
import org.mbari.imgfx.roi.*;


public class AddLocalizationEventBuilder {

    private AddLocalizationEventBuilder() {}

    public static AddLocalizationEvent<? extends DataView<? extends Data, ? extends Shape>, ImageView> build(Localization<? extends DataView<? extends Data, ? extends Shape>, ImageView> loc) {
        var dataView = loc.getDataView();
        if (dataView instanceof MarkerView) {
            return new AddMarkerEvent(loc);
        }
        else if (dataView instanceof RectangleView) {
            return new AddRectangleEvent(loc);
        }
        else if (dataView instanceof LineView) {
            return new AddLineEvent(loc);
        }
        return new AddPolygonEvent(loc);
    }
}
