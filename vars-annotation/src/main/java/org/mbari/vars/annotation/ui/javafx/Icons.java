package org.mbari.vars.annotation.ui.javafx;

import javafx.scene.text.Text;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;

/**
 * @author Brian Schlining
 * @since 2019-07-15T14:39:00
 */
public enum Icons {

    ADD(Material2AL.ADD),
    ADD_A_PHOTO(Material2AL.ADD_A_PHOTO),
    ADD_SHOPPING_CART(Material2AL.ADD_SHOPPING_CART),
    ALARM(Material2AL.ALARM),
    ALARM_OFF(Material2AL.ALARM_OFF),
    ARROW_BACK(Material2AL.ARROW_BACK),
    ARROW_FORWARD(Material2AL.ARROW_FORWARD),
    AV_TIMER(Material2AL.AV_TIMER),
    CACHED(Material2AL.CACHED),
    CANCEL(Material2AL.CANCEL),
    CHECK(Material2AL.CHECK),
    CLEAR(Material2AL.CLEAR),
    CLOSE(Material2AL.CLOSE),
    CLOUD_DONE(Material2AL.CLOUD_DONE),
    CLOUD_UPLOAD(Material2AL.CLOUD_UPLOAD),
    CREATE(Material2AL.CREATE),
    DELETE(Material2AL.DELETE),
    DIRECTIONS_BOAT(Material2AL.DIRECTIONS_BOAT),
    EDIT(Material2AL.EDIT),
    EJECT(Material2AL.EJECT),
    EXPOSURE_NEG_1(Material2AL.EXPOSURE_NEG_1),
    EXPOSURE_PLUS_1(Material2AL.EXPOSURE_PLUS_1),
    FAST_FORWARD(Material2AL.FAST_FORWARD),
    FAST_REWIND(Material2AL.FAST_REWIND),
    FIBER_NEW(Material2AL.FIBER_NEW),
    FILE_UPLOAD(Material2AL.FILE_UPLOAD),
    FLIP_TO_BACK(Material2AL.FLIP_TO_BACK),
    FLIP_TO_FRONT(Material2AL.FLIP_TO_FRONT),
    FOLDER(Material2AL.FOLDER),
    FORMAT_SHAPES(Material2AL.FORMAT_SHAPES),
    GRAIN(Material2AL.GRAIN),
    GRID_ON(Material2AL.GRID_ON),
    IMAGE(Material2AL.IMAGE),
    IMAGE_SEARCH(Material2AL.IMAGE_SEARCH),
    INSERT_COMMENT(Material2AL.INSERT_COMMENT),
    KEYBOARD_ARROW_RIGHT(Material2AL.KEYBOARD_ARROW_RIGHT),
    LIVE_TV(Material2AL.LIVE_TV),
    LOCK(Material2AL.LOCK),
    LOCK_OPEN(Material2AL.LOCK_OPEN),
    NATURE_PEOPLE(Material2MZ.NATURE_PEOPLE),
    PAUSE(Material2MZ.PAUSE),
    PERSON_ADD(Material2MZ.PERSON_ADD),
    PICTURE_IN_PICTURE(Material2MZ.PICTURE_IN_PICTURE),
    PLAY_ARROW(Material2MZ.PLAY_ARROW),
    REDO(Material2MZ.REDO),
    REFRESH(Material2MZ.REFRESH),
    REMOVE(Material2MZ.REMOVE),
    SAVE(Material2MZ.SAVE),
    SEARCH(Material2MZ.SEARCH),
    SETTINGS(Material2MZ.SETTINGS),
    STOP(Material2MZ.STOP),
    TIMELINE(Material2MZ.TIMELINE),
    UNDO(Material2MZ.UNDO),
    VERTICAL_ALIGN_BOTTOM(Material2MZ.VERTICAL_ALIGN_BOTTOM),
    VIDEO_LIBRARY(Material2MZ.VIDEO_LIBRARY),
    VIEW_COLUMN(Material2MZ.VIEW_COLUMN);

    private Ikon ikon;

    Icons(Ikon ikon) {
        this.ikon = ikon;
    }

    public Text size(int size) {
        FontIcon icon = new FontIcon(ikon);
        icon.setIconSize(size);
        icon.getStyleClass().add("glyph-icon");
        return icon;
    }

    public Text standardSize() {
        return size(30);
    }

    public Ikon getIkon() {
        return ikon;
    }

}
