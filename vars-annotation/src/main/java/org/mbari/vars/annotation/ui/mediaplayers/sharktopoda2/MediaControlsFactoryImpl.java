package org.mbari.vars.annotation.ui.mediaplayers.sharktopoda2;

import javafx.application.Platform;
import javafx.util.Pair;
import org.mbari.vars.annotation.etc.jdk.Loggers;
import org.mbari.vars.annotation.ui.mediaplayers.*;
import org.mbari.vars.annotation.ui.messages.ShowExceptionAlert;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;
import org.mbari.vars.annotation.ui.Initializer;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.events.OpenDoneEvent;
import org.mbari.vars.annotation.ui.mediaplayers.sharktopoda.SettingsPaneImpl;
import org.mbari.vars.annotation.ui.mediaplayers.sharktopoda.SharktopodaSettingsPaneController;
import org.mbari.vars.annotation.ui.mediaplayers.sharktopoda.SharktoptodaControlPane;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoState;
import org.mbari.vcr4j.remote.control.RError;
import org.mbari.vcr4j.remote.control.RState;
import org.mbari.vcr4j.remote.control.RVideoIO;
import org.mbari.vcr4j.remote.control.RemoteControl;
import org.mbari.vcr4j.remote.control.commands.CloseCmd;
import org.mbari.vcr4j.remote.control.commands.OpenCmd;
import org.mbari.vcr4j.remote.control.commands.OpenDoneCmd;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
            RemoteControl remoteControl = null;
            OutgoingController outgoingController = null;
            IncomingController incomingController = null;
            try {
                remoteControl = new RemoteControl.Builder(media.getVideoReferenceUuid())
                        .remotePort(remotePort)
                        .port(localPort)
                        .withStatus(true)
                        .withMonitoring(true)
                        .whenFrameCaptureIsDone(imageCaptureService.getEventBus()::send)
                        .whenOpenIsDone(request -> handleOpenDone(media, request))
                        .build()
                        .get();
                var io = remoteControl.getVideoIO();
                imageCaptureService.setIo(io);

                var sharktopodaState = new SharktopodaState();
                outgoingController = new OutgoingController(toolBox, io, sharktopodaState);
                incomingController = new IncomingController(toolBox, remoteControl, sharktopodaState);
                io.send(new OpenCmd(media.getVideoReferenceUuid(), media.getUri().toURL()));

                final var rc = remoteControl;
                final var out = outgoingController;
                final var in = incomingController;
                return new MediaPlayer<>(media, imageCaptureService, io,
                        () -> {
                            sendClose(io, media.getVideoReferenceUuid());
                            rc.close();
                            in.close();
                            out.close();
                            sharktopodaState.setSelectedLocalizations(null);
                        });
            }
            catch (Exception e) {
                // Release the local UDP port and event bus subscriptions. A leaked
                // RemoteControl keeps the port bound, so every later open gets a
                // PlayerIO that silently fails to bind and never receives anything
                // from Sharktopoda.
                closeQuietly(remoteControl, incomingController, outgoingController);
                log.atError().withCause(e).log("Failed to open media " + media.getUri());
                var i18n = toolBox.getI18nBundle();
                var title = i18n.getString("mediaplayer.sharktopoda2.error.title");
                var header = i18n.getString("mediaplayer.sharktopoda2.error.header");
                var content = i18n.getString("mediaplayer.sharktopoda2.error.content");
                toolBox.getEventBus().send(new ShowExceptionAlert(title, header, content, e));
                var io = new NoopVideoIO(media.getVideoName());
                return new MediaPlayer<>(media, new NoopImageCaptureService(), io, () -> {});
            }
        });
    }

    /**
     * UDP remote protocol immediately acks an 'open ...' command. The receiver subsequently sends
     * an 'open done' message reporting success or failure. On success the receiver is ready for
     * video UUID specific commands, so localization sending is enabled; a failure is surfaced to
     * the user. Commands sent before 'open done' would be rejected by the receiver, so they are
     * not sent; the full localization set is sent once the receiver reports ready.
     */
    private void handleOpenDone(Media media, OpenDoneCmd.Request request) {
        if (!request.getUuid().equals(media.getVideoReferenceUuid())) {
            return;
        }
        if (request.isOk()) {
            toolBox.getEventBus().send(new OpenDoneEvent(request.getUuid()));
            return;
        }
        var cause = request.getCause() == null ? "unknown error" : request.getCause();
        log.atWarn().log("Failed to open " + media.getUri() + " in Sharktopoda: " + cause);
        var i18n = toolBox.getI18nBundle();
        var title = i18n.getString("mediaplayer.sharktopoda2.error.title");
        var header = i18n.getString("mediaplayer.sharktopoda2.error.header");
        var content = i18n.getString("mediaplayer.sharktopoda2.error.content");
        toolBox.getEventBus().send(new ShowExceptionAlert(title, header, content,
                new RuntimeException(cause)));
    }

    /**
     * Send the CloseCmd and wait until it has actually been dispatched. The command
     * subject is serialized: when another thread is mid-send (the monitoring decorator
     * requests the index every 333 ms, holding the emitter loop for a full UDP round
     * trip), send() just queues the command and returns. Tearing down the RemoteControl
     * right away would drop the queued CloseCmd and the video would stay open in
     * Sharktopoda. Subscribers see a command after RVideoIO's internal sender has
     * transmitted it, so observing it here means the datagram is on the wire.
     */
    private void sendClose(RVideoIO io, UUID videoReferenceUuid) {
        if (io.isClosed()) {
            return;
        }
        var dispatched = new CompletableFuture<Void>();
        var disposable = io.getCommandSubject()
                .ofType(CloseCmd.class)
                .subscribe(cmd -> dispatched.complete(null));
        try {
            io.send(new CloseCmd(videoReferenceUuid));
            dispatched.get(5, TimeUnit.SECONDS);
        }
        catch (Exception e) {
            log.atWarn().withCause(e)
                    .log("The close command for " + videoReferenceUuid + " may not have reached the video player");
        }
        finally {
            disposable.dispose();
        }
    }

    private void closeQuietly(RemoteControl remoteControl,
                              IncomingController incomingController,
                              OutgoingController outgoingController) {
        if (incomingController != null) {
            try {
                incomingController.close();
            } catch (Exception e) {
                log.atWarn().withCause(e).log("Failed to close IncomingController");
            }
        }
        if (outgoingController != null) {
            try {
                outgoingController.close();
            } catch (Exception e) {
                log.atWarn().withCause(e).log("Failed to close OutgoingController");
            }
        }
        if (remoteControl != null) {
            try {
                remoteControl.close();
            } catch (Exception e) {
                log.atWarn().withCause(e).log("Failed to close RemoteControl");
            }
        }
    }
}
