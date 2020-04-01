package org.mbari.vars.ui.mediaplayers.sharktopoda.localization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vcr4j.sharktopoda.client.gson.DurationConverter;
import org.mbari.vcr4j.sharktopoda.client.localization.IO;

import java.io.Closeable;
import java.time.Duration;

/**
 * @author Brian Schlining
 * @since 2020-03-05T17:02:00
 */
public class LocalizationController implements Closeable {

    private final IncomingController incomingController;
    private final OutgoingController outgoingController;
    private final IO io;
    public static final String EVENT_SOURCE = LocalizationController.class.getName();

    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .registerTypeAdapter(Duration .class, new DurationConverter())
            .create();

    public LocalizationController(LocalizationSettings settings, UIToolBox toolBox) {
        io = new IO(settings.getIncomingPort(),
                settings.getOutgoingPort(),
                settings.getIncomingTopic(),
                settings.getOutgoingTopic());


        incomingController = new IncomingController(toolBox.getEventBus(), io, gson, toolBox.getData());
        outgoingController = new OutgoingController(toolBox.getEventBus(), io, gson);

    }

    public void close() {
        incomingController.close();
        outgoingController.close();
    }


}
