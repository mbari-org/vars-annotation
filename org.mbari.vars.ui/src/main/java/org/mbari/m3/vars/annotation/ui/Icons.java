package org.mbari.m3.vars.annotation.ui;

import javafx.scene.text.Text;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material.Material;

/**
 * @author Brian Schlining
 * @since 2019-07-15T14:39:00
 */
public enum Icons {

    DELETE(Material.DELETE),
    FLIP_TO_FRONT(Material.FLIP_TO_FRONT),
    INSERT_COMMENT(Material.INSERT_COMMENT);

    private Ikon ikon;

    Icons(Ikon ikon) {
        this.ikon = ikon;
    }

    public Text size(int size) {
        FontIcon icon = new FontIcon(ikon);
        icon.setIconSize(size);
        return icon;
    }

}
