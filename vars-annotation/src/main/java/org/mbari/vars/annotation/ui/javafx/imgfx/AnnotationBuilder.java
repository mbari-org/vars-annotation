package org.mbari.vars.annotation.ui.javafx.imgfx;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Shape;
import org.mbari.imgfx.etc.rx.events.AddLocalizationEvent;
import org.mbari.imgfx.etc.rx.events.RemoveLocalizationEvent;
import org.mbari.imgfx.roi.*;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annosaurus.sdk.r1.models.Association;
import org.mbari.vars.annotation.ui.commands.CreateAnnotationAtIndexWithAssociationCmd;
import org.mbari.vars.annotation.ui.commands.CreateAssociationsCmd;
import org.mbari.vcr4j.VideoIndex;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class AnnotationBuilder {
    private final IFXToolBox toolBox;
    private final BooleanProperty localizeExistingAnnotation = new SimpleBooleanProperty();
    private final BooleanProperty addComment = new SimpleBooleanProperty();

    private final RoiBoundingBox roiBoundingBox = new RoiBoundingBox();
    private final RoiLine roiLine = new RoiLine();
    private final RoiMarker roiMarker = new RoiMarker();
    private final RoiPolygon roiPolygon = new RoiPolygon();

    public AnnotationBuilder(IFXToolBox toolBox) {
        this.toolBox = toolBox;
        init();
    }

    private void init() {
        toolBox.getEventBus()
                .toObserverable()
                .ofType(AddLocalizationEvent.class)
                .subscribe(event -> addNewLocalization(event.localization()));
    }

    private void addNewLocalization(Localization<? extends DataView<? extends Data, ? extends Shape>, ImageView> localization) {
        var image = toolBox.getData().getSelectedImage();
        if (image != null) {
            // If needed, show comment dialog
            String comment = addComment.get() ? showCommentDialog().orElse(null) :
                    null;
            if (localizeExistingAnnotation.get()) {
                var annos = new ArrayList<>(toolBox.getUIToolBox()
                        .getData()
                        .getSelectedAnnotations());
                if (annos.size() == 1) {
                    // Add localization to selected annotation.
                    var annotation = annos.get(0);
                    addToExistingAnnotation(annotation, localization, image.getImageReferenceUuid(), comment);
                } else {
                    // If no annotation is selected, or more than one is
                    // fire a new RemoveLocalizationEvent to trash it.
                    toolBox.getEventBus().publish(new RemoveLocalizationEvent(localization));
                    // TODO show warning dialog to let user know to select one annotation
                }
            } else {
                // Create new annotation and association
                createNewAnnotation(localization, image.getImageReferenceUuid(), comment, image.toVideoIndex());
            }
        }
    }

    private Optional<String> showCommentDialog() {
        var i18n = toolBox.getUIToolBox().getI18nBundle();
        var dialog = new TextInputDialog();
        dialog.setTitle(i18n.getString("ifx.comment.dialog.title"));
        dialog.setHeaderText("ifx.comment.dialog.header");
        dialog.setContentText("ifx.comment.dialog.content");
        return dialog.showAndWait();
    }

    private void showNoAnnotationSelectedDialog() {

    }

    private void addToExistingAnnotation(Annotation annotation,
                                         Localization<? extends DataView<? extends Data, ? extends Shape>, ImageView> localization,
                                         UUID imageReferenceUuid,
                                         String comment) {

        localization.setLabel(annotation.getConcept());
        var association = fromLocalization(localization, imageReferenceUuid, comment);
        if (association != null) {
            var command = new CreateAssociationsCmd(association, List.of(annotation));
            toolBox.getUIToolBox().getEventBus().send(command);
        }

    }

    private void createNewAnnotation(
            Localization<? extends DataView<? extends Data, ? extends Shape>, ImageView> localization,
            UUID imageReferenceUuid,
            String comment,
            VideoIndex videoIndex) {

        var association = fromLocalization(localization, imageReferenceUuid, comment);
        if (association != null) {
            var command = new CreateAnnotationAtIndexWithAssociationCmd(videoIndex,
                    localization.getLabel(),
                    association);
            toolBox.getUIToolBox().getEventBus().send(command);
        }

    }

    private Association fromLocalization(Localization<? extends DataView<? extends Data, ? extends Shape>, ImageView> localization,
                                         UUID imageReferenceUuid,
                                         String comment) {

        var dataView = localization.getDataView();

        if (dataView instanceof MarkerView) {
            var loc = (Localization<MarkerView, ImageView>) localization;
            return roiMarker.fromLocalization(loc, imageReferenceUuid, comment);
        }
        else if (dataView instanceof LineView) {
            var loc = (Localization<LineView, ImageView>) localization;
            return roiLine.fromLocalization(loc, imageReferenceUuid, comment);
        }
        else if (dataView  instanceof RectangleView) {
            var loc = (Localization<RectangleView, ImageView>) localization;
            return roiBoundingBox.fromLocalization(loc, imageReferenceUuid, comment);
        }
        else if (dataView instanceof PolygonView) {
            var loc = (Localization<PolygonView, ImageView>) localization;
            return roiPolygon.fromLocalization(loc, imageReferenceUuid, comment);
        }
        return null;

    }



    public boolean isLocalizeExistingAnnotation() {
        return localizeExistingAnnotation.get();
    }

    public BooleanProperty localizeExistingAnnotationProperty() {
        return localizeExistingAnnotation;
    }

    public void setLocalizeExistingAnnotation(boolean localizeExistingAnnotation) {
        this.localizeExistingAnnotation.set(localizeExistingAnnotation);
    }

    public boolean isAddComment() {
        return addComment.get();
    }

    public BooleanProperty addCommentProperty() {
        return addComment;
    }

    public void setAddComment(boolean addComment) {
        this.addComment.set(addComment);
    }
}
