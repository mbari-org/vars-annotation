package org.mbari.vars.ui.javafx.buttons;

import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.commands.CopyAnnotationsCmd;
import org.mbari.vars.ui.events.AnnotationsSelectedEvent;
import org.mbari.vars.ui.mediaplayers.MediaPlayer;
import org.mbari.vars.ui.messages.CopyAnnotationMsg;
import org.mbari.vars.ui.javafx.Icons;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Media;
import org.mbari.vars.services.model.User;
import org.mbari.vars.core.util.AsyncUtils;
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
        Text icon = Icons.FLIP_TO_FRONT.standardSize();
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
