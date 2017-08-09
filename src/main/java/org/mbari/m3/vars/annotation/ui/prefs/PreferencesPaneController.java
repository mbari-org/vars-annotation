package org.mbari.m3.vars.annotation.ui.prefs;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.mediaplayers.sharktopoda.SharktopodaPaneController;

/**
 * @author Brian Schlining
 * @since 2017-08-08T16:25:00
 */
public class PreferencesPaneController {

    private TabPane root;
    private SharktopodaPaneController sharkController;
    private final UIToolBox toolBox;

    public PreferencesPaneController(UIToolBox toolBox) {
        this.toolBox = toolBox;
    }

    public TabPane getRoot() {
        if (root == null) {
            root = new TabPane();

            String sharkName = toolBox.getI18nBundle().getString("mediaplayer.sharktopoda.name");
            Tab sharkTab = new Tab(sharkName);
            sharkTab.setContent(getSharkController().getRoot());
            root.getTabs().add(sharkTab);


        }
        return root;
    }

    private SharktopodaPaneController getSharkController()  {
        if (sharkController == null) {
            sharkController = SharktopodaPaneController.newInstance();
        }
        return sharkController;
    }
}
