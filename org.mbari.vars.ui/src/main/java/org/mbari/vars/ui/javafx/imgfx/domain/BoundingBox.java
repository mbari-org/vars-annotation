package org.mbari.vars.ui.javafx.imgfx.domain;

import org.mbari.imgfx.roi.RectangleData;

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
}

