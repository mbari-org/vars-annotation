package org.mbari.vars.ui.swing;

import org.kordamp.ikonli.swing.FontIcon;
import org.mbari.vars.ui.swing.annotable.Colors;

import java.awt.Color;

public class JIcons {

    private JIcons() {
        // no instantiation
    }

    public static FontIcon asSwing(org.mbari.vars.ui.javafx.Icons icon, int size, Color color) {
        var c = color == null ? Colors.DEFAULT_TEXT.getColor() : color;
        return FontIcon.of(icon.getIkon(), size, c);
    }


}
