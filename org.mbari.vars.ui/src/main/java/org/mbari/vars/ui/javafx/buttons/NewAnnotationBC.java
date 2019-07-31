package org.mbari.vars.ui.javafx.buttons;


import io.reactivex.Observable;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.commands.CreateAnnotationFromConceptCmd;
import org.mbari.vars.ui.events.MediaPlayerChangedEvent;
import org.mbari.vars.ui.mediaplayers.MediaPlayer;
import org.mbari.vars.ui.messages.ClearCacheMsg;
import org.mbari.vars.ui.messages.NewAnnotationMsg;
import org.mbari.vars.ui.javafx.Icons;
import org.mbari.vars.services.model.Media;
import org.mbari.vars.services.model.User;
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

        // Listen for things other than the button to trigger a new annotation
        toolBox.getEventBus()
                .toObserverable()
                .ofType(NewAnnotationMsg.class)
                .subscribe(m -> apply());
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
//        Text icon = iconFactory.createIcon(MaterialIcon.FIBER_NEW, "30px");
        Text icon = Icons.FIBER_NEW.standardSize();
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
