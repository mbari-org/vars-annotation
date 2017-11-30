package org.mbari.m3.vars.annotation.ui.buttons;

import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.commands.CopyAnnotationsCmd;
import org.mbari.m3.vars.annotation.events.AnnotationsSelectedEvent;
import org.mbari.m3.vars.annotation.mediaplayers.MediaPlayer;
import org.mbari.m3.vars.annotation.messages.CopyAnnotationMsg;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.model.User;
import org.mbari.m3.vars.annotation.util.AsyncUtils;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoState;

/**
 * @author Brian Schlining
 * @since 2017-08-28T12:43:00
 */
public class CopyAnnotationBC extends AbstractBC {


    public CopyAnnotationBC(Button button, UIToolBox toolBox) {
        super(button, toolBox);
    }

    public void init() {
        String tooltip = toolBox.getI18nBundle().getString("buttons.copy");
        MaterialIconFactory iconFactory = MaterialIconFactory.get();
        Text icon = iconFactory.createIcon(MaterialIcon.FLIP_TO_FRONT, "30px");
        initializeButton(tooltip, icon);

        toolBox.getEventBus()
                .toObserverable()
                .ofType(AnnotationsSelectedEvent.class)
                .subscribe(e -> {
                    User user = toolBox.getData().getUser();
                    MediaPlayer<? extends VideoState, ? extends VideoError> mediaPlayer = toolBox.getMediaPlayer();
                    boolean enabled = (user != null) && (mediaPlayer != null) && e.get().size() > 0;
                    button.setDisable(!enabled);
                });

        toolBox.getEventBus()
                .toObserverable()
                .ofType(CopyAnnotationMsg.class)
                .subscribe(m -> apply());
    }

    protected void apply() {
        ObservableList<Annotation> annotations = toolBox.getData().getSelectedAnnotations();
        Media media = toolBox.getData().getMedia();
        User user = toolBox.getData().getUser();
        String activity = toolBox.getData().getActivity();

        MediaPlayer<? extends VideoState, ? extends VideoError> mediaPlayer = toolBox.getMediaPlayer();
        if (mediaPlayer != null) {

            AsyncUtils.observe(mediaPlayer.requestVideoIndex())
                    .subscribe(vi -> toolBox.getEventBus()
                            .send(new CopyAnnotationsCmd(media.getVideoReferenceUuid(),
                                    vi, user.getUsername(), activity, annotations)));

        }
    }

    @Override
    protected void checkEnable() {
        // Do nothing
    }
}
