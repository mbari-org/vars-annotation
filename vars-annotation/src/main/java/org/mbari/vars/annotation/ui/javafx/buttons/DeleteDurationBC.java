package org.mbari.vars.ui.javafx.buttons;

import javafx.scene.control.Button;
import javafx.scene.text.Text;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.commands.DeleteDurationCmd;
import org.mbari.vars.ui.javafx.Icons;

import java.util.List;
import java.util.stream.Collectors;

public class DeleteDurationBC extends AbstractBC {

    public DeleteDurationBC(Button button, UIToolBox toolBox) {
        super(button, toolBox);
    }

    @Override
    protected void apply() {
        List<Annotation> annotations = toolBox.getData()
                .getSelectedAnnotations()
                .stream()
                .filter(a -> a.getDuration() != null)
                .collect(Collectors.toList());
        if (!annotations.isEmpty()) {
            toolBox.getEventBus()
                    .send(new DeleteDurationCmd(annotations));
        }
    }

    @Override
    protected void init() {
        String tooltip = toolBox.getI18nBundle().getString("buttons.duration.delete");
        Text icon = Icons.ALARM_OFF.standardSize();
        initializeButton(tooltip, icon);
    }
}
