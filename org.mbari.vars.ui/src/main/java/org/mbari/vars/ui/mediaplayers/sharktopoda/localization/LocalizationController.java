package org.mbari.vars.ui.mediaplayers.sharktopoda.localization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vcr4j.sharktopoda.client.gson.DurationConverter;
import org.mbari.vcr4j.sharktopoda.client.localization.IO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.time.Duration;

/**
 * Wrapper around the IO, IncomingController, and OutgoingController.
 * @author Brian Schlining
 * @since 2020-03-05T17:02:00
 */
public class LocalizationController implements Closeable {

    private final IncomingController incomingController;
    private final OutgoingController outgoingController;
    private final IO io;
    public static final String EVENT_SOURCE = LocalizationController.class.getName();
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .registerTypeAdapter(Duration .class, new DurationConverter())
            .create();

    /**
     * Constructor
     * @param settings Defines the ports to be used
     * @param toolBox Full of all the goodies needed to constructor controllers
     */
    public LocalizationController(LocalizationSettings settings, UIToolBox toolBox) {
        log.debug("Creating new instance using " + gson.toJson(settings));
        io = new IO(settings.getIncomingPort(),
                settings.getOutgoingPort(),
                settings.getIncomingTopic(),
                settings.getOutgoingTopic());

        incomingController = new IncomingController(toolBox.getEventBus(), io, gson, toolBox.getData());
        outgoingController = new OutgoingController(toolBox.getEventBus(), io, gson);

    }

    /**
     * Free resources. Important to call this when you're done
     */
    public void close() {
        incomingController.close();
        outgoingController.close();
        io.close();
    }


}
