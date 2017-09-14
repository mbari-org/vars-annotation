package org.mbari.m3.vars.annotation.ui.buttons;

import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import io.reactivex.Observable;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.commands.CreateAnnotationFromConceptCmd;
import org.mbari.m3.vars.annotation.events.MediaChangedEvent;
import org.mbari.m3.vars.annotation.events.MediaPlayerChangedEvent;
import org.mbari.m3.vars.annotation.events.UserChangedEvent;
import org.mbari.m3.vars.annotation.mediaplayers.MediaPlayer;
import org.mbari.m3.vars.annotation.messages.ClearCacheMsg;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.model.User;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoState;

/**
 * @author Brian Schlining
 * @since 2017-08-22T15:25:00
 */
public class NewAnnotationBC extends AbstractBC {
    private String defaultConceptName;

    public NewAnnotationBC(Button button, UIToolBox toolBox) {
        super(button, toolBox);
        loadDefaultConcept();
        toolBox.getEventBus()
                .toObserverable()
                .ofType(ClearCacheMsg.class)
                .subscribe(m -> loadDefaultConcept());
    }

    private void loadDefaultConcept() {
        button.setDisable(true);
        defaultConceptName = null;
        toolBox.getServices()
                .getConceptService()
                .findRoot()
                .thenAccept(concept -> {
                    defaultConceptName = concept.getName();
                    checkEnable();
                });
    }

    protected void init() {

        String tooltip = toolBox.getI18nBundle().getString("buttons.newnumber");
        MaterialIconFactory iconFactory = MaterialIconFactory.get();
        Text icon = iconFactory.createIcon(MaterialIcon.FIBER_NEW, "30px");
        initializeButton(tooltip, icon);

        Observable<Object> observable = toolBox.getEventBus().toObserverable();
        observable.ofType((MediaPlayerChangedEvent.class))
                .subscribe(m -> checkEnable());
    }

    @Override
    protected void apply() {
        toolBox.getEventBus()
                .send(new CreateAnnotationFromConceptCmd(defaultConceptName));
    }

    @Override
    protected void checkEnable() {
        MediaPlayer<? extends VideoState, ? extends VideoError> mediaPlayer = toolBox.getMediaPlayer();
        Media media = toolBox.getData().getMedia();
        User user = toolBox.getData().getUser();
        boolean enable = defaultConceptName != null && mediaPlayer != null && media != null && user != null;
        button.setDisable(!enable);
    }


}
