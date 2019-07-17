package org.mbari.m3.vars.annotation.ui.buttons;

import javafx.scene.control.Button;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.commands.CreateAssociationsCmd;
import org.mbari.m3.vars.annotation.ui.Icons;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;
import org.mbari.vars.services.model.Media;
import org.mbari.vars.ui.services.CachedReferenceNumberDecorator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Brian Schlining
 * @since 2017-09-12T15:30:00
 */
public class NewReferenceNumberBC extends AbstractBC {

    private final String associationKey;
    private final CachedReferenceNumberDecorator decorator;


    public NewReferenceNumberBC(Button button,
                                UIToolBox toolBox,
                                CachedReferenceNumberDecorator decorator) {
        super(button, toolBox);
        this.associationKey = toolBox.getConfig()
                .getString("app.annotation.identity.reference");
        this.decorator = decorator;
    }

    protected void init() {
        String tooltip = toolBox.getI18nBundle().getString("buttons.newnumber");
//        Text icon = iconFactory.createIcon(MaterialIcon.EXPOSURE_PLUS_1, "30px");
        Text icon = Icons.EXPOSURE_PLUS_1.standardSize();
        initializeButton(tooltip, icon);
    }

    public void apply() {
        Media media = toolBox.getData().getMedia();
        List<Annotation> selected = new ArrayList<>(toolBox.getData().getSelectedAnnotations());
        decorator.findNewReferenceNumbers(media)
                .thenAccept(as -> {
                    int i = associationsToMaxNumber(as) + 1;
                    Association a = new Association(associationKey, Association.VALUE_SELF, i + "");
                    toolBox.getEventBus()
                            .send(new CreateAssociationsCmd(a, selected));
                });

    }

    private int associationsToMaxNumber(List<Association> as) {
        return as.stream()
                .mapToInt(ass -> {
                    try {
                        return Integer.parseInt(ass.getLinkValue());
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);
    }

}
