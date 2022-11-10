package org.mbari.vars.services.model;

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

    public static String LINK_NAME = "bounding box";

    public BoundingBox() {
    }

    public BoundingBox(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public BoundingBox(int x, int y, int width, int height, String generator) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.generator = generator;
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
