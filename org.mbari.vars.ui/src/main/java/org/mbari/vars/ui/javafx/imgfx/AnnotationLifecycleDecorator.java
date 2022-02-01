package org.mbari.vars.ui.javafx.imgfx;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import javafx.scene.image.ImageView;
import javafx.scene.shape.Shape;
import org.mbari.imgfx.AutoscalePaneController;
import org.mbari.imgfx.etc.rx.events.RemoveLocalizationEvent;
import org.mbari.imgfx.imageview.editor.Localizations;
import org.mbari.imgfx.roi.Data;
import org.mbari.imgfx.roi.DataView;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;

import org.mbari.vars.ui.javafx.imgfx.domain.VarsLocalization;
import org.mbari.vars.ui.javafx.imgfx.events.AddLocalizationEventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AnnotationLifecycleDecorator {

    private record RoiAssociation(Annotation annotation, Association association) {}

    private final IFXToolBox toolBox;
    private final AutoscalePaneController<ImageView> autoscalePaneController;
    private final Localizations localizations;
    private final ObservableList<Annotation> annotationsForSelectedImage = FXCollections.observableArrayList();
    private final ObservableList<RoiAssociation> roiAssociations = FXCollections.observableArrayList();
    private static final Logger log = LoggerFactory.getLogger(AnnotationLifecycleDecorator.class);
    public static final Map<String, Roi<? extends DataView<? extends Data, ? extends Shape>>> LINK_VALUES_TO_ROI_MAP = Map.of(
            RoiBoundingBox.LINK_NAME, new RoiBoundingBox(),
            RoiLine.LINK_NAME, new RoiLine(),
            RoiMarker.LINK_NAME, new RoiMarker(),
            RoiPolygon.LINK_NAME, new RoiPolygon()
    );

    public static final List<String> LINK_NAMES_FOR_LOCALIZATIONS = LINK_VALUES_TO_ROI_MAP.keySet().stream().toList();


    public AnnotationLifecycleDecorator(IFXToolBox toolBox,
                                        AutoscalePaneController<ImageView> autoscalePaneController,
                                        Localizations localizations) {
        this.toolBox = toolBox;
        this.autoscalePaneController = autoscalePaneController;
        this.localizations = localizations;
        init();
    }

    private void init() {

        var annotations = toolBox.getUIToolBox().getData().getAnnotations();

        annotations.addListener((ListChangeListener<? super Annotation>) c -> {

            log.debug("Annotations have been changed");
            var newAnnos = new ArrayList<>(annotations.filtered(a -> {
                var image = toolBox.getData().getSelectedImage();
                if (image != null) {
                    return a.getImagedMomentUuid().equals(image.getImagedMomentUuid());
                }
                return false;
            }));

            annotationsForSelectedImage.setAll(newAnnos);

            var rois = newAnnos.stream()
                    .flatMap(a ->
                        a.getAssociations()
                                .stream()
                                .filter(ass -> LINK_NAMES_FOR_LOCALIZATIONS.contains(ass.getLinkName()))
                                .map(ass -> new RoiAssociation(a, ass)))
                    .collect(Collectors.toList());
            roiAssociations.setAll(rois);

        });

        roiAssociations.addListener((ListChangeListener<? super RoiAssociation>) c -> {
            log.debug("RoiAssociations have been changed");
            var locs = roiAssociations
                    .stream()
                    .map(roi -> VarsLocalization.from(roi.annotation(),
                            roi.association(),
                            autoscalePaneController))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            toolBox.getData()
                    .getVarsLocalizations()
                    .setAll(locs);
        });
    }

    private void addVarsLocalizationToView(VarsLocalization vloc) {
        var eventBus = toolBox.getEventBus();
        var match = localizations.getLocalizations()
                .stream()
                .filter(loc -> loc.getUuid().equals(vloc.getLocalization().getUuid()))
                .findFirst();

        // If a localization with the same UUID already exists remove it first
        if (match.isPresent()) {
            eventBus.publish(new RemoveLocalizationEvent(match.get()));
        }
        eventBus.publish(AddLocalizationEventBuilder.build(vloc.getLocalization()));
    }

    private void removeVarsLocalizationFromView(VarsLocalization vloc) {
        var eventBus = toolBox.getEventBus();
        eventBus.publish(new RemoveLocalizationEvent(vloc.getLocalization()));
    }



}
