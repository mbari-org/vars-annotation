package org.mbari.vars.ui.mediaplayers.sharktopoda.localization;

/**
 * Bean class used for serializing/deserializing bounding box info into JSON
 * All coordinaates are intended to be pixels
 */
public class BoundingBox {
    private int x;
    private int y;
    private int width;
    private int height;

    public BoundingBox() {
    }

    public BoundingBox(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
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

}
