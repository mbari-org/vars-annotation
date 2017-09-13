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
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoState;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2017-09-12T15:30:00
 */
public class NewReferenceNumberBC {

    private final String associationKey;
    private final UIToolBox toolBox;
    private final Button button;


    public NewReferenceNumberBC(Button button, UIToolBox toolBox) {
        this.toolBox = toolBox;
        this.button = button;
        this.associationKey = toolBox.getConfig()
                .getString("app.annotation.identity.reference");
        init();
    }

    protected void init() {

        MaterialIconFactory iconFactory = MaterialIconFactory.get();
        Text icon = iconFactory.createIcon(MaterialIcon.EXPOSURE_PLUS_1, "30px");
        button.setText(null);
        button.setGraphic(icon);
        button.setDisable(true);
        button.setOnAction(e -> apply());

        Observable<Object> observable = toolBox.getEventBus().toObserverable();
        observable.ofType(MediaChangedEvent.class)
                .subscribe(m -> checkEnable());
        observable.ofType(UserChangedEvent.class)
                .subscribe(m -> checkEnable());

    }

    public void apply() {
        Media media = toolBox.getData().getMedia();
        UUID videoReferenceUuid = media.getVideoReferenceUuid();
        List<Annotation> selected = new ArrayList<>(toolBox.getData().getSelectedAnnotations());
        toolBox.getServices()
                .getAnnotationService()
                .findByVideoReferenceAndLinkName(videoReferenceUuid, associationKey)
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

    private void checkEnable() {
        Media media = toolBox.getData().getMedia();
        User user = toolBox.getData().getUser();
        boolean enable =  media != null && user != null;
        button.setDisable(!enable);
    }







}
