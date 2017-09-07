package org.mbari.m3.vars.annotation.ui.buttons;

import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import io.reactivex.Observable;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.commands.FramegrabCmd;
import org.mbari.m3.vars.annotation.events.MediaChangedEvent;
import org.mbari.m3.vars.annotation.events.MediaPlayerChangedEvent;
import org.mbari.m3.vars.annotation.events.UserChangedEvent;
import org.mbari.m3.vars.annotation.mediaplayers.MediaPlayer;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.model.User;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoState;

/**
 * @author Brian Schlining
 * @since 2017-09-05T17:06:00
 */
public class FramecaptureBC {

    private final Button button;
    private final UIToolBox toolBox;

    public FramecaptureBC(Button button, UIToolBox toolBox) {
        this.button = button;
        this.toolBox = toolBox;
        init();
    }

    private void init() {
        button.setTooltip(new Tooltip(toolBox.getI18nBundle().getString("buttons.framegrab")));
        MaterialIconFactory iconFactory = MaterialIconFactory.get();
        Text icon = iconFactory.createIcon(MaterialIcon.ADD_A_PHOTO, "30px");
        button.setText(null);
        button.setGraphic(icon);
        button.setDisable(true);
        button.setOnAction(e -> toolBox.getEventBus().send(new FramegrabCmd()));

        Observable<Object> observable = toolBox.getEventBus().toObserverable();
        observable.ofType(MediaChangedEvent.class)
                .subscribe(m -> checkEnable());
        observable.ofType((MediaPlayerChangedEvent.class))
                .subscribe(m -> checkEnable());
        observable.ofType(UserChangedEvent.class)
                .subscribe(m -> checkEnable());
    }

    private void checkEnable() {
        MediaPlayer<? extends VideoState, ? extends VideoError> mediaPlayer = toolBox.getMediaPlayer();
        Media media = toolBox.getData().getMedia();
        User user = toolBox.getData().getUser();
        boolean enable =  mediaPlayer != null && media != null && user != null;
        button.setDisable(!enable);
    }
}
