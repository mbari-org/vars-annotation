package org.mbari.vars.ui.javafx.buttons;


import javafx.scene.control.Button;
import javafx.scene.text.Text;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.commands.SetDurationCmd;
import org.mbari.vars.ui.javafx.Icons;
import org.mbari.vars.services.model.Annotation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Brian Schlining
 * @since 2017-10-23T15:24:00
 */
public class SetDurationBC extends AbstractBC {

    public SetDurationBC(Button button, UIToolBox toolBox) {
        super(button, toolBox);
    }

    @Override
    protected void apply() {
        List<Annotation> annotations = new ArrayList<>(toolBox.getData().getSelectedAnnotations());
        if (!annotations.isEmpty()) {
            toolBox.getEventBus()
                    .send(new SetDurationCmd(annotations));
        }
    }

    @Override
    protected void init() {
        String tooltip = toolBox.getI18nBundle().getString("buttons.duration");
//        Text icon = iconFactory.createIcon(MaterialIcon.AV_TIMER, "30px");
        Text icon = Icons.ALARM.standardSize();
        initializeButton(tooltip, icon);
    }
}
