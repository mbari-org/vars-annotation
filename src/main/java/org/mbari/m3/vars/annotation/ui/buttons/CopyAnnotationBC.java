package org.mbari.m3.vars.annotation.ui.buttons;

import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.commands.CopyAnnotationsCmd;
import org.mbari.m3.vars.annotation.events.AnnotationsSelectedEvent;
import org.mbari.m3.vars.annotation.mediaplayers.MediaPlayer;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.model.User;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoState;

/**
 * @author Brian Schlining
 * @since 2017-08-28T12:43:00
 */
public class CopyAnnotationBC {

    private final Button button;
    private final UIToolBox toolBox;

    public CopyAnnotationBC(Button button, UIToolBox toolBox) {
        this.button = button;
        this.toolBox = toolBox;
        init();
    }

    public void init() {
        button.setTooltip(new Tooltip(toolBox.getI18nBundle().getString("buttons.copy")));
        MaterialIconFactory iconFactory = MaterialIconFactory.get();
        Text icon = iconFactory.createIcon(MaterialIcon.FLIP_TO_FRONT, "30px");
        button.setText(null);
        button.setGraphic(icon);
        button.setDisable(true);

        toolBox.getEventBus()
                .toObserverable()
                .ofType(AnnotationsSelectedEvent.class)
                .subscribe(e -> {
                    User user = toolBox.getData().getUser();
                    MediaPlayer<? extends VideoState, ? extends VideoError> mediaPlayer = toolBox.getMediaPlayer();
                    boolean enabled = (user != null) && (mediaPlayer != null) && e.get().size() > 0;
                    button.setDisable(!enabled);
                });

        button.setOnAction(e -> apply());
    }

    private void apply() {
        ObservableList<Annotation> annotations = toolBox.getData().getSelectedAnnotations();
        Media media = toolBox.getData().getMedia();
        User user = toolBox.getData().getUser();
        MediaPlayer<? extends VideoState, ? extends VideoError> mediaPlayer = toolBox.getMediaPlayer();
        if (mediaPlayer != null) {
            mediaPlayer.requestVideoIndex()
                    .thenAccept(vi -> toolBox.getEventBus()
                                .send(new CopyAnnotationsCmd(media.getVideoReferenceUuid(),
                                        vi, user.getUsername(), annotations)));
        }
    }
}
