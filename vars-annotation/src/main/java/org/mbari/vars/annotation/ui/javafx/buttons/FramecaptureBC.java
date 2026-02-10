package org.mbari.vars.annotation.ui.javafx.buttons;


import io.reactivex.rxjava3.core.Observable;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.commands.FramegrabCmd;
import org.mbari.vars.annotation.ui.events.MediaPlayerChangedEvent;
import org.mbari.vars.annotation.ui.mediaplayers.MediaPlayer;
import org.mbari.vars.annotation.ui.messages.FramecaptureMsg;
import org.mbari.vars.annotation.ui.javafx.Icons;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;
import org.mbari.vars.oni.sdk.r1.models.User;
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
//        Text icon = iconFactory.createIcon(MaterialIcon.ADD_A_PHOTO, "30px");
        Text icon = Icons.ADD_A_PHOTO.standardSize();
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
