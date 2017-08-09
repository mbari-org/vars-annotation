package org.mbari.m3.vars.annotation.mediaplayers;

import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.events.MediaChangedEvent;
import org.mbari.vcr4j.sharktopoda.SharktopodaVideoIO;

import java.net.SocketException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.UUID;

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
                SharktopodaVideoIO videoIO = new SharktopodaVideoIO(UUID.randomUUID(),
                        "localhost", 0);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }

    }
}
