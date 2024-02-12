package org.mbari.vars.services.impl.varsuserserver.v1;

//import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.mbari.vars.services.gson.ByteArrayConverter;
import org.mbari.vars.services.gson.DurationConverter;
import org.mbari.vars.services.gson.InstantConverter;
import org.mbari.vars.services.gson.TimecodeConverter;
import org.mbari.vars.services.RetrofitServiceFactory;
import org.mbari.vcr4j.time.Timecode;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.Duration;
import java.time.Instant;

/**
 * @author Brian Schlining
 * @since 2017-06-27T10:10:00
 */
public class PrefWebServiceFactory extends RetrofitServiceFactory {

    @Inject
    public PrefWebServiceFactory(@Named("PREFS_ENDPOINT") String endpoint,
                                 @Named("PREFS_TIMEOUT") Duration timeout) {
        super(endpoint, timeout);
    }

    public Gson getGson() {
        return newGson();
    }

    public static Gson newGson() {
        GsonBuilder gsonBuilder = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .registerTypeAdapter(Duration.class, new DurationConverter())
                .registerTypeAdapter(Timecode.class, new TimecodeConverter())
                .registerTypeAdapter(Instant.class, new InstantConverter())
                .registerTypeAdapter(byte[].class, new ByteArrayConverter());

        return gsonBuilder.create();

        // Register java.time.Instant
//        return Converters.registerInstant(gsonBuilder)
//                .create();
    }
}