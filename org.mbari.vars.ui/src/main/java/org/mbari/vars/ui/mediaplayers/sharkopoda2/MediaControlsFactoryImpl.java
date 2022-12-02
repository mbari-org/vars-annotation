package org.mbari.vars.ui.mediaplayers.sharkopoda2;

import javafx.util.Pair;
import org.mbari.vars.services.model.Media;
import org.mbari.vars.ui.mediaplayers.MediaControls;
import org.mbari.vars.ui.mediaplayers.MediaControlsFactory;
import org.mbari.vars.ui.mediaplayers.MediaPlayer;
import org.mbari.vars.ui.mediaplayers.SettingsPane;
import org.mbari.vars.ui.mediaplayers.sharktopoda.SharktopodaSettingsPaneController;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoState;
import org.mbari.vcr4j.remote.control.RError;
import org.mbari.vcr4j.remote.control.RState;
import org.mbari.vcr4j.remote.control.RemoteControl;

import java.util.concurrent.CompletableFuture;

public class MediaControlsFactoryImpl implements MediaControlsFactory {

    @Override
    public SettingsPane getSettingsPane() {
        return null;
    }

    @Override
    public boolean canOpen(Media media) {
        boolean b = false;
        if (media != null) {
            String u = media.getUri().toString();
            b =  u.startsWith("http") || u.startsWith("file");
        }
        return b;
    }

    @Override
    public CompletableFuture<MediaControls<? extends VideoState, ? extends VideoError>> open(Media media) {
        Pair<Integer, Integer> portNumbers = SharktopodaSettingsPaneController.getPortNumbers();

        return null;
    }

    public CompletableFuture<MediaPlayer<RState, RError>> open(Media media, int remotePort, int localPort) {
        return CompletableFuture.supplyAsync(() -> {
            var remoteControl = new RemoteControl.Builder(media.getVideoReferenceUuid())
                    .remotePort(remotePort)
                    .port(localPort)
                    .withStatus(true)
                    .withMonitoring(true)
                    .whenFrameCaptureIsDone(cmd -> {
                    })
                    .build();
            return null;
        });
    }
}
