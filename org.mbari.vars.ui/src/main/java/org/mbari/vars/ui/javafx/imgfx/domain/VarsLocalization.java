package org.mbari.vars.ui.javafx.imgfx.domain;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Shape;
import org.mbari.imgfx.AutoscalePaneController;
import org.mbari.imgfx.etc.rx.events.AddLocalizationEvent;
import org.mbari.imgfx.roi.Data;
import org.mbari.imgfx.roi.DataView;
import org.mbari.imgfx.roi.Localization;
import org.mbari.imgfx.roi.RectangleView;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;
import org.mbari.vars.ui.javafx.imgfx.AnnotationLifecycleDecorator;
import org.mbari.vars.ui.javafx.imgfx.LookupUtil;
import org.mbari.vars.ui.javafx.imgfx.Roi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class VarsLocalization {
    private final Annotation annotation;
    private final Association association;
    private final Localization<? extends DataView<? extends Data, ? extends Shape>, ImageView> localization;
    private BooleanProperty dirtyConcept = new SimpleBooleanProperty(false);
    private BooleanProperty dirtyLocalization = new SimpleBooleanProperty(false);
    private static final Logger log = LoggerFactory.getLogger(VarsLocalization.class);


    public VarsLocalization(Annotation annotation,
                            Association association,
                            Localization<? extends DataView<? extends Data, ? extends Shape>, ImageView> localization) {
        this.annotation = annotation;
        this.association = association;
        this.localization = localization;
        init();
    }

    private void init() {
        localization.labelProperty().addListener((obs, oldv, newv) -> dirtyConcept.set(true));
        if (localization.getDataView() instanceof RectangleView view) {
            view.getData().xProperty().addListener((obs, oldv, newv) -> dirtyLocalization.set(true));
            view.getData().yProperty().addListener((obs, oldv, newv) -> dirtyLocalization.set(true));
            view.getData().widthProperty().addListener((obs, oldv, newv) -> dirtyLocalization.set(true));
            view.getData().heightProperty().addListener((obs, oldv, newv) -> dirtyLocalization.set(true));
        }
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public Association getAssociation() {
        return association;
    }

    public Localization getLocalization() {
        return localization;
    }

    public boolean getDirtyConcept() {
        return dirtyConcept.get();
    }

    public BooleanProperty dirtyConceptProperty() {
        return dirtyConcept;
    }

    public void setDirtyConcept(boolean dirtyConcept) {
        this.dirtyConcept.set(dirtyConcept);
    }

    public boolean isDirtyLocalization() {
        return dirtyLocalization.get();
    }

    public BooleanProperty dirtyLocalizationProperty() {
        return dirtyLocalization;
    }

    public void setDirtyLocalization(boolean dirtyLocalization) {
        this.dirtyLocalization.set(dirtyLocalization);
    }

    public static Optional<VarsLocalization> from(Annotation annotation,
                                                  Association association,
                                                  AutoscalePaneController<ImageView> autoscalePaneController) {
        if (LookupUtil.LINK_VALUES_TO_ROI_MAP.containsKey(association.getLinkName())) {
            log.debug("Found ROI in " + association);
            // verify that the annotation contains the association
//            var ok = annotation.getAssociations()
//                    .stream()
//                    .anyMatch(a -> a.getUuid().equals(association.getUuid()));

//            if (ok) {
                final Roi<? extends DataView<? extends org.mbari.imgfx.roi.Data, ? extends Shape>> roi = LookupUtil.LINK_VALUES_TO_ROI_MAP.get(association.getLinkName());
                log.debug("Building ROI using " + roi);
                return roi.fromAssociation(annotation.getConcept(),
                                association,
                                autoscalePaneController)
                        .map(localization -> {
                            // IMPORTANT: the localizations and it's association have the same UUID!!!
                            localization.setUuid(association.getUuid());
                            return new VarsLocalization(annotation, association, localization);
                        });
//            }
//            else {
//                log.warn("Annotation {} does not contain Association {}",
//                        annotation.getObservationUuid(),
//                        association.getUuid());
//            }
        }
        return Optional.empty();
    }


}
