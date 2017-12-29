package org.mbari.m3.vars.annotation.mediaplayers.sharktopoda;

import javafx.scene.layout.Pane;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.mediaplayers.SettingsPane;

/**
 * @author Brian Schlining
 * @since 2017-12-28T13:19:00
 */
public class SettingsPaneImpl implements SettingsPane {

    private final SharktopodaSettingsPaneController controller = SharktopodaSettingsPaneController.newInstance();
    private final String name;

    public SettingsPaneImpl(UIToolBox toolBox) {
        name = toolBox.getI18nBundle().getString("mediaplayer.sharktopoda.name");
    }

    @Override
    public void load() {
        controller.load();
    }

    @Override
    public void save() {
        controller.save();
    }

    @Override
    public Pane getPane() {
        return controller.getRoot();
    }

    @Override
    public String getName() {
        return name;
    }
}
