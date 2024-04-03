package org.mbari.vars.ui.javafx.prefs;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.javafx.mlstage.MLSettingsPaneController;
import org.mbari.vars.ui.javafx.raziel.RazielSettingsPaneController;
//import org.mbari.vars.ui.mediaplayers.sharktopoda.localization.LocalizationSettingsPaneController;
import org.mbari.vars.ui.mediaplayers.MediaPlayers;
import org.mbari.vars.ui.mediaplayers.SettingsPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
            addTab(RazielSettingsPaneController.newInstance());
//            addTab(LocalizationSettingsPaneController.newInstance(toolBox));
            addTab(MLSettingsPaneController.newInstance());
            loadMediaControlsSettingsPanes();
        }
        return root;
    }

    private void addTab(SettingsPane controller) {
        var tab = new Tab(controller.getName());
        tab.setClosable(false);
        tab.setContent(controller.getPane());
        root.getTabs().add(tab);
        prefs.add(controller);
    }

    private void loadMediaControlsSettingsPanes() {
        MediaPlayers mediaPlayers = new MediaPlayers(toolBox);
        List<SettingsPane> settingsPanes = mediaPlayers.getSettingsPanes();
        settingsPanes.stream()
                .filter(Objects::nonNull)
                .forEach(this::addTab);
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
