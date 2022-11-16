package org.mbari.vars.services.model;

import java.util.UUID;
import static org.mbari.vars.core.util.MathUtil.doubleToInt;

/**
 * Bean class used for serializing/deserializing bounding box info into JSON
 * All coordinaates are intended to be pixels
 */
public class BoundingBox {
    private int x;
    private int y;
    private int width;
    private int height;
    private String generator;
    private UUID imageReferenceUuid;
    private String project;

    public static String LINK_NAME = "bounding box";

    public BoundingBox() {
    }

    public BoundingBox(int x, int y, int width, int height) {
        this(x, y, width, height, null);
    }

    public BoundingBox(int x, int y, int width, int height, String generator) {
        this(x, y, width, height, generator, null);
    }

    public BoundingBox(int x, int y, int width, int height, String generator, UUID imageReferenceUuid) {
        this(x, y, width, height, generator, imageReferenceUuid, null);
    }

    public BoundingBox(int x, int y, int width, int height, String generator, UUID imageReferenceUuid, String project) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.generator = generator;
        this.imageReferenceUuid = imageReferenceUuid;
        this.project = project;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getGenerator() {
        return generator;
    }

    public UUID getImageReferenceUuid() {
        return imageReferenceUuid;
    }

    public String getProject() {
        return project;
    }




    @Override
    public String toString() {
        return "BoundingBox[" +
                "x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                ", generator=" + generator +
                ']';
    }
}
