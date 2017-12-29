package org.mbari.m3.vars.annotation.ui.prefs;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.mediaplayers.sharktopoda.SharktopodaSettingsPaneController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Brian Schlining
 * @since 2017-08-08T16:25:00
 */
public class PreferencesPaneController implements IPrefs {

    private TabPane root;
    private SharktopodaSettingsPaneController sharkController;
    private final UIToolBox toolBox;
    private final List<IPrefs> prefs = new ArrayList<>();

    public PreferencesPaneController(UIToolBox toolBox) {
        this.toolBox = toolBox;
    }

    public TabPane getRoot() {
        if (root == null) {
            root = new TabPane();

            String sharkName = toolBox.getI18nBundle().getString("mediaplayer.sharktopoda.name");
            Tab sharkTab = new Tab(sharkName);
            sharkTab.setClosable(false);
            sharkTab.setContent(getSharkController().getRoot());
            root.getTabs().add(sharkTab);

        }
        return root;
    }

    private SharktopodaSettingsPaneController getSharkController()  {
        if (sharkController == null) {
            sharkController = SharktopodaSettingsPaneController.newInstance();
            prefs.add(sharkController);
        }
        return sharkController;
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
