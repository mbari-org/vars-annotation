package org.mbari.vars.annotation.ui.javafx.buttons;

import javafx.scene.control.Button;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.javafx.Icons;
import org.mbari.vars.annotation.ui.javafx.mlstage.ApplyMLToVideoCmd;

public class MachineLearningBC extends AbstractBC {

    public MachineLearningBC(Button button, UIToolBox toolBox) {
        super(button, toolBox);
    }

    @Override
    protected void apply() {
        toolBox.getEventBus().send(new ApplyMLToVideoCmd());
    }

    @Override
    protected void init() {
        var tooltip = toolBox.getI18nBundle().getString("buttons.ml");
        var icon = Icons.IMAGE_SEARCH.standardSize();
        initializeButton(tooltip, icon);
    }
}
