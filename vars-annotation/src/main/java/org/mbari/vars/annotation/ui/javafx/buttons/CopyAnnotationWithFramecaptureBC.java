package org.mbari.vars.annotation.ui.javafx.buttons;

import io.reactivex.rxjava3.core.Observable;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.commands.CopyAnnotationsWithFramegrabCmd;
import org.mbari.vars.annotation.ui.events.AnnotationsSelectedEvent;
import org.mbari.vars.annotation.ui.events.MediaPlayerChangedEvent;
import org.mbari.vars.annotation.ui.javafx.Icons;
import org.mbari.vars.annotation.ui.mediaplayers.MediaPlayer;
import org.mbari.vars.annotation.ui.messages.CopyAnnotationWithFramecaptureMsg;
import org.mbari.vars.annotation.ui.util.JFXUtilities;
import org.mbari.vars.oni.sdk.r1.models.User;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoState;

/**
 * Combines the actions of {@link CopyAnnotationBC} and {@link FramecaptureBC}: captures a
 * framegrab and copies the selected annotations onto it.
 *
 * @author Brian Schlining
 * @since 2026-10-08
 */
public class CopyAnnotationWithFramecaptureBC extends AbstractBC {

    public CopyAnnotationWithFramecaptureBC(Button button, UIToolBox toolBox) {
        super(button, toolBox);
    }

    protected void init() {
        String tooltip = toolBox.getI18nBundle().getString("buttons.copyframegrab");
        Text icon = Icons.ADD_PHOTO_ALTERNATE.standardSize();
        initializeButton(tooltip, icon);

        Observable<Object> observable = toolBox.getEventBus().toObserverable();
        observable.ofType(MediaPlayerChangedEvent.class)
                .subscribe(m -> checkEnable());
        observable.ofType(AnnotationsSelectedEvent.class)
                .subscribe(e -> checkEnable());

        // Listen for things other than the button to trigger a copy with framegrab
        observable.ofType(CopyAnnotationWithFramecaptureMsg.class)
                .subscribe(m -> apply());
    }

    @Override
    protected void checkEnable() {
        MediaPlayer<? extends VideoState, ? extends VideoError> mediaPlayer = toolBox.getMediaPlayer();
        Media media = toolBox.getData().getMedia();
        User user = toolBox.getData().getUser();
        boolean hasSelection = !toolBox.getData().getSelectedAnnotations().isEmpty();
        boolean enable = mediaPlayer != null && media != null && user != null && hasSelection;
        JFXUtilities.runOnFXThread(() -> button.setDisable(!enable));
    }

    @Override
    protected void apply() {
        ObservableList<Annotation> annotations = toolBox.getData().getSelectedAnnotations();
        User user = toolBox.getData().getUser();
        if (annotations.isEmpty() || user == null) {
            return;
        }
        String activity = toolBox.getData().getActivity();
        toolBox.getEventBus()
                .send(new CopyAnnotationsWithFramegrabCmd(user.getUsername(), activity, annotations));
    }
}
