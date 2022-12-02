package org.mbari.vars.ui.mediaplayers.sharktopoda;

import javafx.util.Pair;
import org.mbari.vars.ui.Initializer;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.mediaplayers.MediaControls;
import org.mbari.vars.ui.mediaplayers.MediaPlayer;
import org.mbari.vars.ui.mediaplayers.MediaControlsFactory;
import org.mbari.vars.ui.mediaplayers.SettingsPane;
import org.mbari.vars.services.model.Media;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoIO;
import org.mbari.vcr4j.VideoState;
import org.mbari.vcr4j.decorators.SchedulerVideoIO;
import org.mbari.vcr4j.decorators.StatusDecorator;
import org.mbari.vcr4j.decorators.VideoSyncDecorator;
import org.mbari.vcr4j.sharktopoda.SharktopodaError;
import org.mbari.vcr4j.sharktopoda.SharktopodaState;
import org.mbari.vcr4j.sharktopoda.SharktopodaVideoIO;
import org.mbari.vcr4j.sharktopoda.commands.OpenCmd;
import org.mbari.vcr4j.sharktopoda.commands.SharkCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

/**
 * @author Brian Schlining
 * @since 2017-12-28T12:38:00
 */
public class MediaControlsFactoryImpl implements MediaControlsFactory {

    private final UIToolBox toolBox;
    private SettingsPaneImpl settingsPane;
    private SharktoptodaControlPane controlPane;
    private final Logger log = LoggerFactory.getLogger(getClass());

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
        return open(media, portNumbers.getKey(), portNumbers.getValue())
                .thenApply(mediaPlayer -> {
                    controlPane.setMediaPlayer(mediaPlayer);
                    return new MediaControls<>(mediaPlayer, controlPane);
                });
    }

    public CompletableFuture<MediaPlayer<SharktopodaState, SharktopodaError>> open(Media media, int sharktopodaPort, int framecapturePort) {
        CompletableFuture<MediaPlayer<SharktopodaState, SharktopodaError>> cf = new CompletableFuture<>();

        // Spawn this off in a thread or on an executor
        Runnable r = () -> {
            try {
                SharktopodaVideoIO videoIO = new SharktopodaVideoIO(media.getVideoReferenceUuid(), "localhost", sharktopodaPort);
                videoIO.send(new OpenCmd(media.getUri().toURL()));
                VideoIO<SharktopodaState, SharktopodaError> io =
                        new SchedulerVideoIO<>(videoIO, Executors.newCachedThreadPool());
                StatusDecorator<SharktopodaState, SharktopodaError> statusDecorator = new StatusDecorator<>(io);
                VideoSyncDecorator<SharktopodaState, SharktopodaError> syncDecorator = new VideoSyncDecorator<>(io);
                MediaPlayer<SharktopodaState, SharktopodaError> newVc =
                        new MediaPlayer<>(media, new ImageCaptureServiceImpl(videoIO, framecapturePort),
                                io,
                                () -> {
                                    io.send(SharkCommands.CLOSE);
                                    statusDecorator.unsubscribe();
                                    syncDecorator.unsubscribe();
                                 });
                cf.complete(newVc);
                io.send(SharkCommands.SHOW);
            } catch (Exception e) {
                log.error("Failed to create SharktopodaVideoIO", e);
                cf.completeExceptionally(e);
            }
        };
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.start();

        return cf;
    }
}
