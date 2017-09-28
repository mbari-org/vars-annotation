package org.mbari.m3.vars.annotation.services.varskbserver.v1;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.mbari.m3.vars.annotation.gson.ByteArrayConverter;
import org.mbari.m3.vars.annotation.gson.DurationConverter;
import org.mbari.m3.vars.annotation.gson.TimecodeConverter;
import org.mbari.m3.vars.annotation.services.RetrofitServiceFactory;
import org.mbari.vcr4j.time.Timecode;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.Duration;
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
        GsonBuilder gsonBuilder = new GsonBuilder()
                .setPrettyPrinting()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .registerTypeAdapter(Duration.class, new DurationConverter())
                .registerTypeAdapter(Timecode.class, new TimecodeConverter())
                .registerTypeAdapter(byte[].class, new ByteArrayConverter());

        // Register java.time.Instant
        return Converters.registerInstant(gsonBuilder).create();

    }

}
