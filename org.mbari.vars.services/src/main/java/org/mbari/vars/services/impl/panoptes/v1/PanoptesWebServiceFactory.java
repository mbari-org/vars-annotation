package org.mbari.vars.services.impl.panoptes.v1;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.mbari.vars.services.gson.ByteArrayConverter;
import org.mbari.vars.services.gson.DurationConverter;
import org.mbari.vars.services.gson.TimecodeConverter;
import org.mbari.vars.services.RetrofitServiceFactory;
import org.mbari.vcr4j.time.Timecode;

import javax.inject.Named;
import java.time.Duration;

/**
 * @author Brian Schlining
 * @since 2017-08-31T13:54:00
 */
public class PanoptesWebServiceFactory extends RetrofitServiceFactory {

    public PanoptesWebServiceFactory(@Named("PANOPTES_ENDPOINT") String endpoint,
                                     @Named("PANOPTES_TIMEOUT") Duration timeout) {
        super(endpoint, timeout);
    }

    @Override
    public Gson getGson() {
        return newGson();
    }

    public static Gson newGson() {
        GsonBuilder gsonBuilder = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .registerTypeAdapter(Duration.class, new DurationConverter())
                .registerTypeAdapter(Timecode.class, new TimecodeConverter())
                .registerTypeAdapter(byte[].class, new ByteArrayConverter());

        // Register java.time.Instant
        return Converters.registerInstant(gsonBuilder)
                .create();
    }
}
