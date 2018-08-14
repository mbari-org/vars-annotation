package org.mbari.m3.vars.annotation.ui.buttons;

import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import io.reactivex.Observable;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.commands.FramegrabCmd;
import org.mbari.m3.vars.annotation.events.MediaPlayerChangedEvent;
import org.mbari.m3.vars.annotation.mediaplayers.MediaPlayer;
import org.mbari.m3.vars.annotation.messages.FramecaptureMsg;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.model.User;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoState;

/**
 * @author Brian Schlining
 * @since 2017-09-05T17:06:00
 */
public class FramecaptureBC extends AbstractBC {


    public FramecaptureBC(Button button, UIToolBox toolBox) {
        super(button, toolBox);
    }

    protected void init() {
        String tooltip = toolBox.getI18nBundle().getString("buttons.framegrab");
        MaterialIconFactory iconFactory = MaterialIconFactory.get();
        Text icon = iconFactory.createIcon(MaterialIcon.ADD_A_PHOTO, "30px");
        initializeButton(tooltip, icon);
        Observable<Object> observable = toolBox.getEventBus().toObserverable();
        observable.ofType(MediaPlayerChangedEvent.class)
                .subscribe(m -> checkEnable());

        // FIXME: Framecapture will fail if the annotation already has an image
        // as the database will not except duplicate

        // Listen for things other than the button to trigger a new annotation
        toolBox.getEventBus()
                .toObserverable()
                .ofType(FramecaptureMsg.class)
                .subscribe(m -> apply());
    }

    @Override
    protected void checkEnable() {
        MediaPlayer<? extends VideoState, ? extends VideoError> mediaPlayer = toolBox.getMediaPlayer();
        Media media = toolBox.getData().getMedia();
        User user = toolBox.getData().getUser();
        boolean enable =  mediaPlayer != null && media != null && user != null;
        button.setDisable(!enable);
    }

    @Override
    protected void apply() {
        toolBox.getEventBus().send(new FramegrabCmd());
    }
}
