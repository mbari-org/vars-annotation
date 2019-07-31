package org.mbari.vars.ui.demos.javafx.prefs;

import com.jfoenix.controls.JFXTabPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.demos.mediaplayers.MediaPlayers;
import org.mbari.vars.ui.demos.mediaplayers.SettingsPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Brian Schlining
 * @since 2017-08-08T16:25:00
 */
public class PreferencesPaneController implements IPrefs {

    private JFXTabPane root;
    private final UIToolBox toolBox;
    private final List<IPrefs> prefs = new ArrayList<>();

    public PreferencesPaneController(UIToolBox toolBox) {
        this.toolBox = toolBox;
    }

    public TabPane getRoot() {
        if (root == null) {
            root = new JFXTabPane();
            root.setPrefSize(600, 600);
            loadMediaControlsSettingsPanes();
        }
        return root;
    }

    private void loadMediaControlsSettingsPanes() {
        MediaPlayers mediaPlayers = new MediaPlayers(toolBox);
        List<SettingsPane> settingsPanes = mediaPlayers.getSettingsPanes();
        settingsPanes.stream()
                .filter(Objects::nonNull)
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
        getRoot(); // Loads preferences panes
        prefs.forEach(IPrefs::load);
    }

    /**
     * Saves prefs for each time
     */
    @Override
    public void save() {
        getRoot(); // Loads preferences panes
        prefs.forEach(IPrefs::save);
    }

}
