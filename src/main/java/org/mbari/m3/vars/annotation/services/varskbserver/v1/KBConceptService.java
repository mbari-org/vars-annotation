package org.mbari.m3.vars.annotation.services.varskbserver.v1;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.mbari.m3.vars.annotation.gson.ByteArrayConverter;
import org.mbari.m3.vars.annotation.gson.DurationConverter;
import org.mbari.m3.vars.annotation.gson.TimecodeConverter;
import org.mbari.m3.vars.annotation.model.Concept;
import org.mbari.m3.vars.annotation.model.ConceptAssociationTemplate;
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



    /** Underlying retrofit API service */
    private final KBService service;

    /**
     * Constructor for the vars-kb-service interface.
     * @param endpoint The endpoint of the vars-kb-service. e.g. http://m3.shore.mbari.org/kb/v1
     */
    public KBConceptService(String endpoint) {
        String correctEndpoint = (endpoint.endsWith("/")) ? endpoint : endpoint + "/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(correctEndpoint)
                .addConverterFactory(GsonConverterFactory.create(getGson()))
                .build();
        service = retrofit.create(KBService.class);
    }


    @Override
    public CompletableFuture<Concept> fetchConceptTree() {
        // f1 finds the root name. f2 then looks up the tree using that root
        CompletableFuture<ConceptDetails> f1 = new CompletableFuture<>();
        service.findRootDetails().enqueue(newCallback(f1));
        return f1.thenCompose(root -> {
            CompletableFuture<Concept> f2 = new CompletableFuture<>();
            service.findTree(root.getName()).enqueue(newCallback(f2));
            return f2;
        });
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
        service.listConceptNames().enqueue(newCallback(f));
        return f;
    }

    @Override
    public CompletableFuture<List<ConceptAssociationTemplate>> findTemplates(String name) {
        CompletableFuture<List<ConceptAssociationTemplate>> f = new CompletableFuture<>();
        service.findTemplates(name).enqueue(newCallback(f));
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

    /**
     * Factory method to create a Retrofit Callback that completes the service provided as an arg.
     * @param future The future to be completed by the Callback
     * @param <T> The return type of the future
     * @return A Retrofit Callback.
     */
    private static <T> Callback<T> newCallback(CompletableFuture<T> future) {
        return new Callback<T>() {
            @Override
            public void onResponse(Response<T> response) {
                future.complete(response.body());
            }

            @Override
            public void onFailure(Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        };
    }
}
