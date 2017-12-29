package org.mbari.m3.vars.annotation.mediaplayers;

import javafx.scene.layout.Pane;
import org.mbari.m3.vars.annotation.ui.prefs.IPrefs;

/**
 * @author Brian Schlining
 * @since 2017-12-28T13:06:00
 */
public interface SettingsPane extends IPrefs {

    String getName();

    Pane getPane();
}
