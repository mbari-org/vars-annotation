package org.mbari.vars.annotation.ui.mediaplayers.sharktopoda2;

import javafx.application.Platform;
import javafx.util.Pair;
import org.mbari.vars.annotation.etc.jdk.Loggers;
import org.mbari.vars.annotation.ui.mediaplayers.*;
import org.mbari.vars.annotation.ui.messages.ShowExceptionAlert;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;
import org.mbari.vars.annotation.ui.Initializer;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.mediaplayers.sharktopoda.SettingsPaneImpl;
import org.mbari.vars.annotation.ui.mediaplayers.sharktopoda.SharktopodaSettingsPaneController;
import org.mbari.vars.annotation.ui.mediaplayers.sharktopoda.SharktoptodaControlPane;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoState;
import org.mbari.vcr4j.remote.control.RError;
import org.mbari.vcr4j.remote.control.RState;
import org.mbari.vcr4j.remote.control.RemoteControl;
import org.mbari.vcr4j.remote.control.commands.CloseCmd;
import org.mbari.vcr4j.remote.control.commands.OpenCmd;

import java.util.concurrent.CompletableFuture;

public class MediaControlsFactoryImpl implements MediaControlsFactory {

    private final Loggers log = new Loggers(getClass());

    private final ImageCaptureServiceImpl imageCaptureService = new ImageCaptureServiceImpl();
    private SharktoptodaControlPane controlPane;
    private final UIToolBox toolBox;
    private SettingsPaneImpl settingsPane;

    public MediaControlsFactoryImpl() {
        this.toolBox = Initializer.getToolBox();
        controlPane = new SharktoptodaControlPane(toolBox);
    }

    @Override
    public SettingsPane getSettingsPane() {
        if (settingsPane == null) {
            settingsPane = new SettingsPaneImpl(toolBox);
        }
        return settingsPane;
    }

    @Override
    public boolean canOpen(Media media) {
        // Check to see if the user has specified sharktopoda version 2 in settings
        boolean b = false;
        if (media != null) {
            var version = SharktopodaSettingsPaneController.getSharktopodaVersion();
            if (version == 2) {
                String u = media.getUri().toString();
                b = u.startsWith("http") || u.startsWith("file");
            }
        }
        return b;
    }

    @Override
    public CompletableFuture<MediaControls<? extends VideoState, ? extends VideoError>> open(Media media) {
        Pair<Integer, Integer> portNumbers = SharktopodaSettingsPaneController.getPortNumbers();
        return open(media, portNumbers.getKey(), portNumbers.getValue())
                .thenApply(mediaPlayer -> {
                    Platform.runLater(() -> controlPane.setMediaPlayer(mediaPlayer));
                    return new MediaControls<>(mediaPlayer, controlPane);
                });
    }

    public CompletableFuture<MediaPlayer<RState, RError>> open(Media media, int remotePort, int localPort) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var remoteControl = new RemoteControl.Builder(media.getVideoReferenceUuid())
                        .remotePort(remotePort)
                        .port(localPort)
                        .withStatus(true)
                        .withMonitoring(true)
                        .whenFrameCaptureIsDone(imageCaptureService.getEventBus()::send)
                        .build()
                        .get();
                var io = remoteControl.getVideoIO();
                imageCaptureService.setIo(io);

                var sharktopodaState = new SharktopodaState();
                var outgoingController = new OutgoingController(toolBox, io, sharktopodaState);
                var incomingController = new IncomingController(toolBox, remoteControl, sharktopodaState);
                try {
                    io.send(new OpenCmd(media.getVideoReferenceUuid(), media.getUri().toURL()));
                } catch (Exception e) {
                    throw new RuntimeException("Failed to open " + media.getUri(), e);
                }

                return new MediaPlayer<>(media, imageCaptureService, io,
                        () -> {
                            io.send(new CloseCmd(media.getVideoReferenceUuid()));
                            remoteControl.close();
                            incomingController.close();
                            outgoingController.close();
                            sharktopodaState.setSelectedLocalizations(null);
                        });
            }
            catch (Exception e) {
                log.atError().withCause(e).log("Failed to open media " + media.getUri());
                var i18n = toolBox.getI18nBundle();
                var title = i18n.getString("mediaplayers.sharktopoda2.error.title");
                var header = i18n.getString("mediaplayers.sharktopoda2.error.header");
                var content = i18n.getString("mediaplayers.sharktopoda2.error.content");
                toolBox.getEventBus().send(new ShowExceptionAlert(title, header, content, e));
                var io = new NoopVideoIO(media.getVideoName());
                return new MediaPlayer<>(media, new NoopImageCaptureService(), io, () -> {});
            }

//            var remoteControl = new RemoteControl.Builder(media.getVideoReferenceUuid())
//                    .remotePort(remotePort)
//                    .port(localPort)
//                    .withStatus(true)
//                    .withMonitoring(true)
//                    .whenFrameCaptureIsDone(imageCaptureService.getEventBus()::send)
//                    .build()
//                    .get();
//            var io = remoteControl.getVideoIO();
//            imageCaptureService.setIo(io);
//
//
//            var sharktopodaState = new SharktopodaState();
//            var outgoingController = new OutgoingController(toolBox, io, sharktopodaState);
//            var incomingController = new IncomingController(toolBox, remoteControl, sharktopodaState);
//            try {
//                io.send(new OpenCmd(media.getVideoReferenceUuid(), media.getUri().toURL()));
//            }
//            catch (Exception e) {
//                throw new RuntimeException("Failed to open " + media.getUri(), e);
//            }
//
//            return new MediaPlayer<>(media, imageCaptureService, io,
//                    () -> {
//                        io.send(new CloseCmd(media.getVideoReferenceUuid()));
//                        remoteControl.close();
//                        incomingController.close();
//                        outgoingController.close();
//                        sharktopodaState.setSelectedLocalizations(null);
//                    });

        });
    }
}
