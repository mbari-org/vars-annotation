package org.mbari.vars.services.impl.varskbserver.v1;

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
import java.util.concurrent.Executor;

/**
 * @author Brian Schlining
 * @since 2017-05-26T11:10:00
 */
public class KBWebServiceFactory extends RetrofitServiceFactory {


    @Inject
    public KBWebServiceFactory(@Named("CONCEPT_ENDPOINT") String endpoint,
            @Named("CONCEPT_TIMEOUT") Duration timeout,
            @Named("CONCEPT_EXECUTOR")Executor executor) {
        super(endpoint, timeout, executor);
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
//        return Converters.registerInstant(gsonBuilder).create();
    }

}
