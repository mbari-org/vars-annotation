package org.mbari.vars.ui.javafx.mlstage;

import javafx.scene.image.ImageView;
import org.mbari.imgfx.Autoscale;
import org.mbari.imgfx.AutoscalePaneController;
import org.mbari.imgfx.roi.Localization;
import org.mbari.imgfx.roi.RectangleView;
import org.mbari.vars.services.model.Image;
import org.mbari.vars.services.model.MachineLearningLocalization;

import java.util.Optional;

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
}
