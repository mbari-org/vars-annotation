package org.mbari.vars.annotation.ui.mediaplayers.vcr;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.mbari.vars.annotation.etc.vcr4j.NoopVideoIO;
import org.mbari.vars.annotation.etc.vcr4j.NoopVideoState;
import org.mbari.vars.annotation.ui.Initializer;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.mediaplayers.*;
import org.mbari.vars.annotation.ui.messages.ShowInfoAlert;
import org.mbari.vars.annotation.ui.messages.ShowNonfatalErrorAlert;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;
import org.mbari.vcr4j.SimpleVideoError;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoIO;
import org.mbari.vcr4j.VideoState;
//import org.mbari.vcr4j.jserialcomm.SerialCommVideoIO;
//import org.mbari.vcr4j.rs422.RS422Error;
//import org.mbari.vcr4j.rs422.RS422State;
//import org.mbari.vcr4j.rs422.decorators.UserbitsAsTimeDecorator;
import org.mbari.vars.annotation.etc.jdk.Loggers;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.prefs.Preferences;

/** VCR's are no longer supported simply for the fact that you can't actually buy them anymore.
 *
 * @author Brian Schlining
 * @since 2018-04-02T14:02:00
 */
public class MediaControlsFactoryImpl implements MediaControlsFactory {

    private final Loggers log = new Loggers(getClass());
    private final UIToolBox toolBox = Initializer.getToolBox();
    public static Class PREF_NODE_KEY = MediaControlsFactoryImpl.class;
    private volatile VideoIO<NoopVideoState, SimpleVideoError> videoIO;

    @Override
    public SettingsPane getSettingsPane() {
        return null;
    }

    @Override
    public boolean canOpen(Media media) {
        return media != null &&
                media.getUri() != null &&
                media.getUri().toString().startsWith(MediaParams.URI_PREFIX);
    }

    @Override
    public CompletableFuture<MediaControls<? extends VideoState, ? extends VideoError>> open(Media media) {

        videoIO = new NoopVideoIO(media.getVideoName());
        var mediaPlayer = new MediaPlayer<>(media, new NoopImageCaptureService(), videoIO);
        return CompletableFuture.completedFuture(new MediaControls<>(mediaPlayer, getPane()));
    }

    private Pane getPane() {
        var pane = new BorderPane();
        pane.setPadding(new Insets(10));
        var text = new Label("VCR Tapes are no longer supported for annotation");
        text.setPadding(new Insets(10));
        text.setFont(Font.font(16));
        text.getStyleClass().add("attention-button");
        pane.setCenter(text);
//        pane.setPrefSize(440, 80);
//        pane.setMaxSize(440, 80);
//        pane.setMinSize(440, 80);
        return pane;
    }

}
