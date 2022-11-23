package org.mbari.vars.ui.swing;

import org.kordamp.ikonli.swing.FontIcon;

import java.awt.Color;

public class JIcons {

    private JIcons() {
        // no instantiation
    }

    public static FontIcon asSwing(org.mbari.vars.ui.javafx.Icons icon, int size, Color color) {
        return FontIcon.of(icon.getIkon(), size, color);
    }


}
