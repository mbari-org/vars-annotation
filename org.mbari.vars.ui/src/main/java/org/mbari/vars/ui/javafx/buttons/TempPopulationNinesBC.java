package org.mbari.vars.ui.javafx.buttons;

import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.commands.CreateAssociationsCmd;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Brian Schlining
 * @since 2017-10-11T11:03:00
 */
public class TempPopulationNinesBC extends AbstractBC {

    public TempPopulationNinesBC(Button button, UIToolBox toolBox) {
        super(button, toolBox);
    }

    @Override
    protected void apply() {
        Association a = new Association("population-quantity",
                Association.VALUE_SELF,
                "999");

        List<Annotation> selectedAnnotations = new ArrayList<>(toolBox.getData().getSelectedAnnotations());
        toolBox.getEventBus()
                .send(new CreateAssociationsCmd(a, selectedAnnotations));
    }

    @Override
    protected void init() {
        String tooltip = "999";
        Text icon = new Text(tooltip);
        icon.setFill(Color.BISQUE);
        DropShadow ds = new DropShadow();
        ds.setOffsetY(3.0F);
        ds.setColor(Color.GOLDENROD);
        icon.setEffect(ds);
        initializeButton(tooltip, icon);
    }
}
