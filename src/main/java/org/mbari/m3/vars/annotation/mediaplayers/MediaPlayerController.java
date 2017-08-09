package org.mbari.m3.vars.annotation.mediaplayers;

import javafx.util.Pair;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.events.MediaChangedEvent;
import org.mbari.m3.vars.annotation.mediaplayers.sharktopoda.SharktopodaPaneController;
import org.mbari.vcr4j.VideoIO;
import org.mbari.vcr4j.decorators.SchedulerVideoIO;
import org.mbari.vcr4j.decorators.StatusDecorator;
import org.mbari.vcr4j.decorators.VCRSyncDecorator;
import org.mbari.vcr4j.sharktopoda.SharktopodaError;
import org.mbari.vcr4j.sharktopoda.SharktopodaState;
import org.mbari.vcr4j.sharktopoda.SharktopodaVideoIO;
import org.mbari.vcr4j.sharktopoda.commands.OpenCmd;
import org.mbari.vcr4j.sharktopoda.decorators.FauxTimecodeDecorator;

import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.concurrent.Executors;

/**
 * @author Brian Schlining
 * @since 2017-08-07T10:50:00
 */
public class MediaPlayerController {

    private final UIToolBox toolBox;

    public MediaPlayerController(UIToolBox toolBox) {
        this.toolBox = toolBox;
        toolBox.getEventBus()
                .toObserverable()
                .ofType(MediaChangedEvent.class)
                .subscribe(e -> open(e.get().getUri()));
    }

    private void open(URI uri) {
        String u = uri.toString();
        if (u.startsWith("urn:tid")) {
            // handle tape
        }
        else if (u.startsWith("http")) {
            // handle file via sharktopoda
            try {
                Pair<Integer, Integer> portNumbers = SharktopodaPaneController.getPortNumbers();
                SharktopodaVideoIO videoIO = new SharktopodaVideoIO(UUID.randomUUID(),
                        "localhost", portNumbers.getKey());
                videoIO.send(new OpenCmd(uri.toURL()));
                new StatusDecorator<>(videoIO);
                new VCRSyncDecorator<>(videoIO, 1000, 100, 3000000);
                new FauxTimecodeDecorator(videoIO); // Convert elapsed-time to timecode
                VideoIO<SharktopodaState, SharktopodaError> io = new SchedulerVideoIO<>(videoIO, Executors.newCachedThreadPool());


                // TODO finish videoIO wiring See vars SharktopodaVideoPlayer
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }
}
