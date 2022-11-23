package org.mbari.vars.ui.swing.annotable;

import java.awt.Color;

public enum Colors {

    ATTENTION(Color.getColor("#FFB74D")),
    CONCURRENT(Color.getColor(" #FDD835")),
    DEFAULT(Color.getColor("#2b3940")),
    DEFAULT_TEXT(Color.getColor("#B3A9A3")),
    EMPHASIS(Color.getColor("#5D3D3D")),
    IMAGE(Color.getColor("#1E88E5")),
    JSON(Color.getColor(" #8E24AA")),
    SAMPLE(Color.getColor("#00600f")),
    TABLE_TEXT(Color.getColor("#79c9ff"));



    private final Color color;

    Colors(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
