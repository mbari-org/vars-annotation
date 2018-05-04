package org.mbari.m3.vars.annotation.ui.rectlabel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.mbari.m3.vars.annotation.UIToolBox;

/**
 * @author Brian Schlining
 * @since 2018-05-04T15:08:00
 */
public class RectLabelStageController {

    private final UIToolBox toolBox;
    private ObservableList images = FXCollections.observableArrayList();

    public RectLabelStageController(UIToolBox toolBox) {
        this.toolBox = toolBox;
    }
}
