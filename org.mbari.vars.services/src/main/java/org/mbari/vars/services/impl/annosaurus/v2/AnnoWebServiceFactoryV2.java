package org.mbari.vars.services.impl.annosaurus.v2;

//import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.FieldNamingPolicy;
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
 * @since 2019-05-14T14:15:00
 */
public class AnnoWebServiceFactoryV2  extends RetrofitServiceFactory {


    @Inject
    public AnnoWebServiceFactoryV2(@Named("ANNO_ENDPOINT_V2") String endpoint, Duration timeout) {
        super(endpoint, timeout);
    }


    public Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder()
                .setPrettyPrinting()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
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
