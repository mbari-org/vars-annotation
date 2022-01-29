package org.mbari.vars.ui.javafx.imgfx.model;

import java.util.List;
import java.util.UUID;

public class Points {
    private List<Integer> x;
    private List<Integer> y;
    private UUID imageReferenceUuuid;
    private String comment;

    public Points() {
    }

    public Points(List<Integer> x, List<Integer> y, UUID imageReferenceUuuid) {
        this(x, y, imageReferenceUuuid, null);
    }

    public Points(List<Integer> x, List<Integer> y, UUID imageReferenceUuuid, String comment) {
        this.x = x;
        this.y = y;
        this.imageReferenceUuuid = imageReferenceUuuid;
        this.comment = comment;
    }

    public List<Integer> getX() {
        return x;
    }

    public void setX(List<Integer> x) {
        this.x = x;
    }

    public List<Integer> getY() {
        return y;
    }

    public void setY(List<Integer> y) {
        this.y = y;
    }

    public UUID getImageReferenceUuuid() {
        return imageReferenceUuuid;
    }

    public void setImageReferenceUuuid(UUID imageReferenceUuuid) {
        this.imageReferenceUuuid = imageReferenceUuuid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
