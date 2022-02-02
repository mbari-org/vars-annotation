package org.mbari.vars.ui.javafx.imgfx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Shape;
import org.mbari.imgfx.AutoscalePaneController;
import org.mbari.imgfx.roi.Data;
import org.mbari.imgfx.roi.DataView;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;
import org.mbari.vars.services.model.Image;
import org.mbari.vars.ui.javafx.imgfx.domain.VarsLocalization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class LookupUtil {

    private static final Logger log = LoggerFactory.getLogger(LookupUtil.class);

    private record RoiAssociation(Annotation annotation, Association association) {}

    public static final Map<String, Roi<? extends DataView<? extends Data, ? extends Shape>>> LINK_VALUES_TO_ROI_MAP = Map.of(
            RoiBoundingBox.LINK_NAME, new RoiBoundingBox(),
            RoiLine.LINK_NAME, new RoiLine(),
            RoiMarker.LINK_NAME, new RoiMarker(),
            RoiPolygon.LINK_NAME, new RoiPolygon()
    );

    public static final List<String> LINK_NAMES_FOR_LOCALIZATIONS = LINK_VALUES_TO_ROI_MAP.keySet().stream().toList();

    private LookupUtil() {}

    /**
     * @return A readonly ObservableList of annotations that have the
     * same imagedMomentUuid as the selected image.
     */
    public static ObservableList<Annotation> getAnnotationsForImage(IFXToolBox toolBox, Image image) {
        if (image == null) {
            return FXCollections.emptyObservableList();
        }
        else {
            var imagedMomentUuid = image.getImagedMomentUuid();
            var annos = toolBox.getUIToolBox()
                    .getData()
                    .getAnnotations()
                    .filtered(a -> a.getImagedMomentUuid().equals(imagedMomentUuid));
            log.debug("Found {} annotations for {}", annos.size(), image.getUrl());
            return annos;
        }
    }

    /**
     * Find the images for the given annotations. If the annotations do not all
     * belong to the same imagedMoment, and empty collection is returned
     * @param annotations The annotations of interest
     * @return The matching collection of images, all with the same imagedMomentUuid.
     *  Otherwise, an empty collection
     */
    public static List<Image> getImagesForAnnotations(IFXToolBox toolBox, Collection<Annotation> annotations) {
        // Make sure they all belong to same imagedMoment
        var uniqueImagedMomentUuids = annotations.stream()
                .map(Annotation::getImagedMomentUuid)
                .distinct()
                .collect(Collectors.toList());
        if (uniqueImagedMomentUuids.size() != 1) {
            return  Collections.emptyList();
        }

        var imagedMomentUuid = uniqueImagedMomentUuids.get(0);
        // Find matching image
        return toolBox.getData()
                .getImages()
                .stream()
                .filter(i -> i.getImagedMomentUuid().equals(imagedMomentUuid))
                .collect(Collectors.toList());

    }

    public static List<VarsLocalization> getVarsLocalizationsForImage(IFXToolBox toolBox,
                                                               Image image,
                                                               AutoscalePaneController<ImageView> autoscalePaneController) {
        var vlocs = getAnnotationsForImage(toolBox, image)
                .stream()
                .flatMap(a -> a.getAssociations()
                        .stream()
                        .filter(ass -> LINK_NAMES_FOR_LOCALIZATIONS.contains(ass.getLinkName()))
                        .map(ass -> new RoiAssociation(a, ass)))
                .map(roi -> VarsLocalization.from(roi.annotation(),
                        roi.association(),
                        autoscalePaneController))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        log.debug("Found {} localizations in annotations for {}", vlocs.size(), image.getUrl());
        return vlocs;
    }

}
