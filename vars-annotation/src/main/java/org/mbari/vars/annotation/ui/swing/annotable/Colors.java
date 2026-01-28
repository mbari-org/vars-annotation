package org.mbari.vars.ui.swing.annotable;

import java.awt.Color;

public enum Colors {

    ATTENTION(Color.decode("#FFB74D")),
    ACTION_ADD(Color.decode("#00AB5F")),
    ACTION_DELETE(Color.decode("#D32F2F")),
    CONCURRENT(Color.decode("#FDD835")),
    DEFAULT(Color.decode("#2b3940")),
    DEFAULT_DARKER(Color.decode("#222d33")),
    DEFAULT_DARKEST(Color.decode("#1c262a")),
    DEFAULT_TEXT(Color.decode("#B3A9A3")),
    DEFAULT_TABLE_TEXT(Color.decode("#79c9ff")),
    EMPHASIS(Color.decode("#5D3D3D")),
    IMAGE(Color.decode("#1E88E5")),
    JSON(Color.decode("#8E24AA")),
    SAMPLE(Color.decode("#00600f")),
    TABLE_TEXT(Color.decode("#79c9ff"));



    private final Color color;

    Colors(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
