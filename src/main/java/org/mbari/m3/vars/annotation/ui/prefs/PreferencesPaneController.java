package org.mbari.m3.vars.annotation.ui.prefs;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.mediaplayers.MediaPlayers;
import org.mbari.m3.vars.annotation.mediaplayers.SettingsPane;
import org.mbari.m3.vars.annotation.mediaplayers.sharktopoda.SharktopodaSettingsPaneController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Brian Schlining
 * @since 2017-08-08T16:25:00
 */
public class PreferencesPaneController implements IPrefs {

    private TabPane root;
    private final UIToolBox toolBox;
    private final List<IPrefs> prefs = new ArrayList<>();

    public PreferencesPaneController(UIToolBox toolBox) {
        this.toolBox = toolBox;
    }

    public TabPane getRoot() {
        if (root == null) {
            root = new TabPane();
            root.setPrefSize(600, 600);
            loadMediaControlsSettingsPanes();
        }
        return root;
    }

    private void loadMediaControlsSettingsPanes() {
        MediaPlayers mediaPlayers = new MediaPlayers(toolBox);
        List<SettingsPane> settingsPanes = mediaPlayers.getSettingsPanes();
        settingsPanes.stream()
                .forEach(settingsPane -> {
                    Tab tab = new Tab(settingsPane.getName());
                    tab.setClosable(false);
                    tab.setContent(settingsPane.getPane());
                    root.getTabs().add(tab);
                    prefs.add(settingsPane);
                });
    }

    /**
     * Loads prefs for each tab
     */
    @Override
    public void load() {
        prefs.forEach(IPrefs::load);
    }

    /**
     * Saves prefs for each time
     */
    @Override
    public void save() {
        prefs.forEach(IPrefs::save);
    }

}
