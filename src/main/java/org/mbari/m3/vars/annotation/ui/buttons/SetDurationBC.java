package org.mbari.m3.vars.annotation.ui.buttons;

import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.commands.SetDurationCmd;
import org.mbari.m3.vars.annotation.model.Annotation;

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
        MaterialIconFactory iconFactory = MaterialIconFactory.get();
        Text icon = iconFactory.createIcon(MaterialIcon.AV_TIMER, "30px");
        initializeButton(tooltip, icon);
    }
}
