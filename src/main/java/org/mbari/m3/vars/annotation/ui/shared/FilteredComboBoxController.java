package org.mbari.m3.vars.annotation.ui.shared;

import javafx.scene.control.ComboBox;

/**
 * @author Brian Schlining
 * @since 2017-08-17T10:59:00
 */
public class FilteredComboBoxController<T>  {

    private final ComboBox<T> comboBox;

    public FilteredComboBoxController() {
        this.comboBox = new ComboBox<>();
    }

    public ComboBox<T> getComboBox() {
        return comboBox;
    }
}
