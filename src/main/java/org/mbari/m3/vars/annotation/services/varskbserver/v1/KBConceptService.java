package org.mbari.m3.vars.annotation.services.varskbserver.v1;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.mbari.m3.vars.annotation.gson.ByteArrayConverter;
import org.mbari.m3.vars.annotation.gson.DurationConverter;
import org.mbari.m3.vars.annotation.gson.TimecodeConverter;
import org.mbari.m3.vars.annotation.model.Concept;
import org.mbari.m3.vars.annotation.model.ConceptDetails;
import org.mbari.m3.vars.annotation.services.ConceptService;
import org.mbari.vcr4j.time.Timecode;
import retrofit2.*;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service that calls the REST API for vampire-squid. This version does NO caching,
 * each call will be sent to the server.
 *
 * @author Brian Schlining
 * @since 2017-05-11T16:13:00
 */
public class KBConceptService implements ConceptService {



    private final KBService service;

    public KBConceptService(String endpoint) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(getGson()))
                .build();
        service = retrofit.create(KBService.class);
    }

    @Override
    public CompletableFuture<Concept> fetchConceptTree() {
        CompletableFuture<Concept> f = new CompletableFuture<>();
        service.findRoot()
                .enqueue(new Callback<Concept>() {
                    @Override
                    public void onResponse(Response<Concept> response) {
                        f.complete(response.body());
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        f.completeExceptionally(throwable);
                    }
                });
        return f;
    }

    @Override
    public CompletableFuture<Optional<ConceptDetails>> findDetails(String name) {
        CompletableFuture<Optional<ConceptDetails>> f = new CompletableFuture<>();
        service.findDetails(name)
                .enqueue(new Callback<ConceptDetails>() {
                    @Override
                    public void onResponse(Response<ConceptDetails> response) {
                        f.complete(Optional.ofNullable(response.body()));
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        f.completeExceptionally(throwable);
                    }
                });

        return f;
    }

    @Override
    public CompletableFuture<List<String>> findAllNames() {
        CompletableFuture<List<String>> f = new CompletableFuture<>();
        service.listConceptNames()
                .enqueue(new Callback<List<String>>() {
                    @Override
                    public void onResponse(Response<List<String>> response) {
                        f.complete(response.body());
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        f.completeExceptionally(throwable);
                    }
                });

        return f;
    }



    private static Gson getGson() {
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
