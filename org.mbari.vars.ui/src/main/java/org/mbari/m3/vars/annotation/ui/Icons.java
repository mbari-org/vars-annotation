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

    ADD(Material.ADD),
    ADD_A_PHOTO(Material.ADD_A_PHOTO),
    ADD_SHOPPING_CART(Material.ADD_SHOPPING_CART),
    AV_TIMER(Material.AV_TIMER),
    CACHED(Material.CACHED),
    CANCEL(Material.CANCEL),
    CLEAR(Material.CLEAR),
    CREATE(Material.CREATE),
    DELETE(Material.DELETE),
    DIRECTIONS_BOAT(Material.DIRECTIONS_BOAT),
    EDIT(Material.EDIT),
    EXPOSURE_NEG_1(Material.EXPOSURE_NEG_1),
    EXPOSURE_PLUS_1(Material.EXPOSURE_PLUS_1),
    FAST_FORWARD(Material.FAST_FORWARD),
    FAST_REWIND(Material.FAST_REWIND),
    FIBER_NEW(Material.FIBER_NEW),
    FLIP_TO_BACK(Material.FLIP_TO_BACK),
    FLIP_TO_FRONT(Material.FLIP_TO_FRONT),
    FOLDER(Material.FOLDER),
    GRID_ON(Material.GRID_ON),
    IMAGE(Material.IMAGE),
    INSERT_COMMENT(Material.INSERT_COMMENT),
    KEYBOARD_ARROW_RIGHT(Material.KEYBOARD_ARROW_RIGHT),
    LIVE_TV(Material.LIVE_TV),
    LOCK(Material.LOCK),
    LOCK_OPEN(Material.LOCK_OPEN),
    NATURE_PEOPLE(Material.NATURE_PEOPLE),
    PAUSE(Material.PAUSE),
    PERSON_ADD(Material.PERSON_ADD),
    PICTURE_IN_PICTURE(Material.PICTURE_IN_PICTURE),
    PLAY_ARROW(Material.PLAY_ARROW),
    REDO(Material.REDO),
    REFRESH(Material.REFRESH),
    REMOVE(Material.REMOVE),
    SAVE(Material.SAVE),
    SEARCH(Material.SEARCH),
    SETTINGS(Material.SETTINGS),
    TIMELINE(Material.TIMELINE),
    UNDO(Material.UNDO),
    VERTICAL_ALIGN_BOTTOM(Material.VERTICAL_ALIGN_BOTTOM),
    VIDEO_LIBRARY(Material.VIDEO_LIBRARY),
    VIEW_COLUMN(Material.VIEW_COLUMN);

    private Ikon ikon;

    Icons(Ikon ikon) {
        this.ikon = ikon;
    }

    public Text size(int size) {
        FontIcon icon = new FontIcon(ikon);
        icon.setIconSize(size);
        return icon;
    }

    public Text standardSize() {
        return size(30);
    }

}
