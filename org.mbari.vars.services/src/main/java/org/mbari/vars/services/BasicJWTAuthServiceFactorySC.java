package org.mbari.vars.services;

//import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.mbari.vars.services.etc.gson.InstantConverter;

import java.time.Duration;
import java.time.Instant;

/**
 * This parses the basic JWT tokens from snake_case JSON.
 * @author Brian Schlining
 * @since 2017-06-27T13:10:00
 */
public class BasicJWTAuthServiceFactorySC extends RetrofitServiceFactory {

    public BasicJWTAuthServiceFactorySC(String endpoint, Duration timeout) {
        super(endpoint, timeout);
    }

    @Override
    public Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder()
                .setPrettyPrinting()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(Instant.class, new InstantConverter())
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        return gsonBuilder.create();

        // Register java.time.Instant
//        return Converters.registerInstant(gsonBuilder)
//                .create();

    }
}
