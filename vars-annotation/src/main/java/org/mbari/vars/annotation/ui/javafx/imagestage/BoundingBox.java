package org.mbari.vars.annotation.ui.javafx.imagestage;

import javafx.scene.image.ImageView;
import org.mbari.imgfx.AutoscalePaneController;
import org.mbari.imgfx.roi.Localization;
import org.mbari.imgfx.roi.RectangleData;
import org.mbari.imgfx.roi.RectangleView;
import org.mbari.vars.annosaurus.sdk.r1.models.Association;

import java.util.Optional;
import java.util.UUID;

public class BoundingBox {
    private Integer x;
    private Integer y;
    private Integer width;
    private Integer height;
    private UUID imageReferenceUuid;
    private String comment;

    public BoundingBox() {
    }

    public BoundingBox(Integer x, Integer y, Integer width, Integer height, UUID imageReferenceUuid) {
        this(x, y, width, height, imageReferenceUuid, null);
    }

    public BoundingBox(Integer x, Integer y, Integer width, Integer height, UUID imageReferenceUuid, String comment) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.imageReferenceUuid = imageReferenceUuid;
        this.comment = comment;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public UUID getImageReferenceUuid() {
        return imageReferenceUuid;
    }

    public void setImageReferenceUuid(UUID imageReferenceUuid) {
        this.imageReferenceUuid = imageReferenceUuid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public RectangleData toData() {
        return new RectangleData(x, y, width, height);
    }

    public static Optional<BoundingBox> fromAssociation(Association association) {
        try {
            var box = Json.GSON.fromJson(association.getLinkValue(), BoundingBox.class);
            return Optional.of(box);
        }
        catch (Exception e) {
            return Optional.empty();
        }
    }

    public static Optional<Localization<RectangleView, ImageView>> fromAssociation(String concept,
                                                                            Association association,
                                                                            AutoscalePaneController<ImageView> paneController) {
        var boundingBox = Json.GSON.fromJson(association.getLinkValue(), BoundingBox.class);
        return RectangleView.fromImageCoords(boundingBox.getX().doubleValue(),
                        boundingBox.getY().doubleValue(),
                        boundingBox.getWidth().doubleValue(),
                        boundingBox.getHeight().doubleValue(),
                        paneController.getAutoscale())
                .map(dataView -> new Localization<>(dataView, paneController, association.getUuid(), concept));
    }
}

