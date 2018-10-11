package org.mbari.m3.vars.annotation.ui.buttons;

import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import io.reactivex.Observable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.commands.CreateAssociationsCmd;
import org.mbari.m3.vars.annotation.events.MediaChangedEvent;
import org.mbari.m3.vars.annotation.events.UserChangedEvent;
import org.mbari.m3.vars.annotation.mediaplayers.MediaPlayer;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.model.User;
import org.mbari.m3.vars.annotation.ui.AnnotationServiceDecorator;
import org.mbari.m3.vars.annotation.ui.AnnotationServiceDecorator2;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoState;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-09-12T15:30:00
 */
public class NewReferenceNumberBC extends AbstractBC {

    private final String associationKey;
    private final AnnotationServiceDecorator2 decorator;


    public NewReferenceNumberBC(Button button, UIToolBox toolBox) {
        super(button, toolBox);
        this.associationKey = toolBox.getConfig()
                .getString("app.annotation.identity.reference");
        this.decorator = new AnnotationServiceDecorator2(toolBox);
    }

    protected void init() {
        String tooltip = toolBox.getI18nBundle().getString("buttons.newnumber");
        MaterialIconFactory iconFactory = MaterialIconFactory.get();
        Text icon = iconFactory.createIcon(MaterialIcon.EXPOSURE_PLUS_1, "30px");
        initializeButton(tooltip, icon);
    }

    public void apply() {
        Media media = toolBox.getData().getMedia();
        List<Annotation> selected = new ArrayList<>(toolBox.getData().getSelectedAnnotations());
        decorator.findReferenceNumberAssociations(media, associationKey)
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
