package org.mbari.vars.annotation.services;

import org.mbari.vars.annosaurus.sdk.r1.AnnosaurusHttpClient;
import org.mbari.vars.annosaurus.sdk.r1.AnnotationService;
import org.mbari.vars.annosaurus.sdk.r1.NoopAnnotationService;
import org.mbari.vars.annotation.etc.jdk.Loggers;
import org.mbari.vars.annotation.services.noop.NoopImageArchiveService;
import org.mbari.vars.annotation.services.panopes.PanoptesHttpClient;
import org.mbari.vars.annotation.services.raziel.Raziel;
import org.mbari.vars.oni.sdk.r1.ConceptService;
import org.mbari.vars.oni.sdk.r1.NoopConceptService;
import org.mbari.vars.oni.sdk.r1.OniKiotaClient;
import org.mbari.vars.raziel.sdk.r1.RazielKiotaClient;
import org.mbari.vars.raziel.sdk.r1.models.EndpointConfig;
import org.mbari.vars.vampiresquid.sdk.r1.MediaService;
import org.mbari.vars.vampiresquid.sdk.r1.NoopMediaService;
import org.mbari.vars.vampiresquid.sdk.r1.VampireSquidKiotaClient;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class ServiceBuilder {

    private final Loggers log = new Loggers(getClass());

    private final boolean load;

    private final List<EndpointConfig> endpoints = new CopyOnWriteArrayList<>();

    private final AtomicReference<AnnosaurusHttpClient> annotationService = new AtomicReference<>();
    private final AtomicReference<VampireSquidKiotaClient> mediaService = new AtomicReference<>();
    private final AtomicReference<OniKiotaClient> conceptService = new AtomicReference<>();
    private final AtomicReference<ImageArchiveService> imageArchiveService = new AtomicReference<>();

    public ServiceBuilder(boolean load) {
        this.load = load;
    }

    private synchronized void loadConfigurations() {
        if (load && endpoints.isEmpty()) {
            try {

                var razielConnectionParams = Raziel.ConnectionParams.load();
                razielConnectionParams.ifPresent((params) -> {
                    var urlString = ServiceBuilder.adaptUrl(params.url().toString());
                    var uri = URI.create(urlString);
                    var client = new RazielKiotaClient(uri);
                    var bearerAuth = client.authenticate(params.username(), params.password()).join();
                    var services = client.endpoints(bearerAuth.accessToken()).join();
                    endpoints.addAll(services);
                });
            }
            catch (Exception e) {
                log.atError().withCause(e).log("Failed to load Raziel connection parameters");
            }
        }
    }

    public synchronized AnnotationService getAnnotationService() {
        if (annotationService.get() == null) {
            loadConfigurations();
            var config = endpoints.stream()
                    .filter(e -> e.name().equals("annosaurus"))
                    .findFirst();
            if (config.isPresent()) {
                var endpoint = config.get();
                var timeout = Duration.ofMillis(endpoint.timeoutMillis());
                var client = new AnnosaurusHttpClient(endpoint.url(), timeout, endpoint.secret());
                annotationService.set(client);
                return client;
            } else {
                log.atWarn().log("No Annosaurus endpoint found");
                return new NoopAnnotationService();
            }
        }
        return annotationService.get();

    }

    public synchronized MediaService getMediaService() {
        if (mediaService.get() == null) {
            loadConfigurations();
            var config = endpoints.stream()
                    .filter(e -> e.name().equals("vampire-squid"))
                    .map(this::adaptEndpointConfigForKiota)
                    .findFirst();
            if (config.isPresent()) {
                var endpoint = config.get();
                var uri = URI.create(endpoint.url());
                var client = new VampireSquidKiotaClient(uri, endpoint.secret());
                mediaService.set(client);
                return client;
            }
            else {
                log.atWarn().log("No Vampire-squid endpoint found");
                return new NoopMediaService();

            }
        }
        return mediaService.get();
    }


    public synchronized ConceptService getConceptService() {
        if (conceptService.get() == null) {
            loadConfigurations();
            var config = endpoints.stream()
                    .filter(e -> e.name().equals("oni"))
                    .map(this::adaptEndpointConfigForKiota)
                    .findFirst();
            if (config.isPresent()) {
                var endpoint = config.get();
                var uri = URI.create(endpoint.url());
                var client = new OniKiotaClient(uri);
                conceptService.set(client);
                return client;
            } else {
                log.atWarn().log("No Oni endpoint found");
                return new NoopConceptService();
            }
        }
        return conceptService.get();
    }

    public synchronized ImageArchiveService getImageArchiveService() {
        if (imageArchiveService.get() == null) {
            loadConfigurations();
            var config = endpoints.stream()
                    .filter(e -> e.name().equals("panoptes"))
                    .findFirst();
            if (config.isPresent()) {
                var endpoint = config.get();
                var uri = URI.create(endpoint.url());
                var timeout = Duration.ofMillis(endpoint.timeoutMillis());
                var client = new PanoptesHttpClient(uri, timeout, endpoint.secret());
                imageArchiveService.set(client);
                return client;
            } else {
                log.atWarn().log("No Panoptes endpoint found");
                return new NoopImageArchiveService();
            }
        }
        return imageArchiveService.get();
    }



    public static String adaptUrl(String url) {
        if (url.endsWith("/config")) {
            return url.substring(0, url.length() - "/config".length());
        }
        else if (url.endsWith("/v1")) {
            return url.substring(0, url.length() - "/v1".length());
        }
        return url;
    }

    public EndpointConfig adaptEndpointConfigForKiota(EndpointConfig endpointConfig) {
        var fixedUrl = adaptUrl(endpointConfig.url());
        return new EndpointConfig(endpointConfig.name(), fixedUrl, endpointConfig.timeoutMillis(), endpointConfig.secret());
    }
}
