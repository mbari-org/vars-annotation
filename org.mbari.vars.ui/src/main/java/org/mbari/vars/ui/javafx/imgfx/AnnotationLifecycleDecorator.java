package org.mbari.vars.ui.javafx.imgfx;

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
import org.mbari.vars.ui.javafx.imgfx.domain.VarsLocalization;
import org.mbari.vars.ui.javafx.imgfx.events.AddLocalizationEventBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AnnotationLifecycleDecorator {

    private final IFXToolBox toolBox;
    private final ObservableList<Annotation> annotationsForSelectedImage;
    private final AutoscalePaneController<ImageView> autoscalePaneController;
    private final Localizations localizations;

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

        // When the annotations for the selected image change, update the corresponding
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

        toolBox.getData()
                .getVarsLocalizations()
                .addListener((ListChangeListener<? super VarsLocalization>) c -> {
                    while (c.next()) {
                        if (c.wasAdded()) {
                            c.getAddedSubList().forEach(this::addVarsLocalizationToView);
                        }
                        else if (c.wasRemoved()) {
                            c.getRemoved().forEach(this::removeVarsLocalizationFromView);
                        }
                    }
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
