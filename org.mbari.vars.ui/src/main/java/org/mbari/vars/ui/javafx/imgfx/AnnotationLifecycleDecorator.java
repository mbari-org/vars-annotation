package org.mbari.vars.ui.javafx.imgfx;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Shape;
import org.mbari.imgfx.AutoscalePaneController;
import org.mbari.imgfx.roi.Data;
import org.mbari.imgfx.roi.DataView;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.ui.javafx.imgfx.domain.VarsLocalization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AnnotationLifecycleDecorator {

    private final IFXToolBox toolBox;
    private final ObservableList<Annotation> annotationsForSelectedImage;
    private final AutoscalePaneController<ImageView> autoscalePaneController;

    public static final Map<String, Roi<? extends DataView<? extends Data, ? extends Shape>>> LINK_VALUES_TO_ROI_MAP = Map.of(
            RoiBoundingBox.LINK_NAME, new RoiBoundingBox(),
            RoiLine.LINK_NAME, new RoiLine(),
            RoiMarker.LINK_NAME, new RoiMarker(),
            RoiPolygon.LINK_NAME, new RoiPolygon()
    );

    public static final List<String> LINK_NAMES_FOR_LOCALIZATIONS = LINK_VALUES_TO_ROI_MAP.keySet().stream().toList();

    public AnnotationLifecycleDecorator(IFXToolBox toolBox, AutoscalePaneController<ImageView> autoscalePaneController) {
        this.toolBox = toolBox;
        this.autoscalePaneController = autoscalePaneController;
        annotationsForSelectedImage = toolBox.getUIToolBox()
                .getData()
                .getAnnotations()
                .filtered(a -> {
                    var image = toolBox.getData().getSelectedImage();
                    if (image != null) {
                        return a.getImagedMomentUuid().equals(image.getImagedMomentUuid());
                    }
                    return false;
                });
        init();
    }

    private void init() {
        annotationsForSelectedImage.addListener((ListChangeListener<? super Annotation>) c -> {
            if (c.next()) {
                var annos = new ArrayList<>(annotationsForSelectedImage);

                var varsLocalizations = annos.stream()
                        .flatMap(anno ->
                                anno.getAssociations()
                                        .stream()
                                        .filter(a -> LINK_NAMES_FOR_LOCALIZATIONS.contains(a.getLinkName()))
                                        .map(a -> VarsLocalization.from(anno, a, autoscalePaneController))
                        )
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
                toolBox.getData().getVarsLocalizations().setAll(varsLocalizations);
            }
        });
    }

    // TODO list for annotation updates from VARS side? and update labels or bounds as needed
}
