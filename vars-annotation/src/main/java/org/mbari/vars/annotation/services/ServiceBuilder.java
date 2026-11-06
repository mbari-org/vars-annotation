package org.mbari.vars.annotation.services;

import com.typesafe.config.ConfigFactory;
import org.mbari.vars.annosaurus.sdk.r1.AnnosaurusHttpClient;
import org.mbari.vars.annosaurus.sdk.r1.AnnotationService;
import org.mbari.vars.annosaurus.sdk.r1.NoopAnnotationService;
import org.mbari.vars.annotation.etc.jdk.Loggers;
import org.mbari.vars.annotation.services.noop.NoopConceptService;
import org.mbari.vars.annotation.services.noop.NoopImageArchiveService;
import org.mbari.vars.annotation.services.panopes.PanoptesHttpClient;
import org.mbari.vars.annotation.services.raziel.Raziel;
import org.mbari.vars.oni.sdk.r1.CachedPreferencesService;
import org.mbari.vars.oni.sdk.r1.ConceptService;
import org.mbari.vars.oni.sdk.r1.OniKiotaClient;
import org.mbari.vars.oni.sdk.r1.PreferencesService;
import org.mbari.vars.raziel.sdk.r1.RazielKiotaClient;
import org.mbari.vars.raziel.sdk.r1.models.EndpointConfig;
import org.mbari.vars.vampiresquid.sdk.r1.MediaService;
import org.mbari.vars.vampiresquid.sdk.r1.NoopMediaService;
import org.mbari.vars.vampiresquid.sdk.r1.VampireSquidKiotaClient;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ServiceBuilder {

    private final Loggers log = new Loggers(getClass());

    private final boolean load;

    private final List<EndpointConfig> endpoints = new CopyOnWriteArrayList<>();

    private final AtomicReference<AnnosaurusHttpClient> annotationService = new AtomicReference<>();
    private final AtomicReference<VampireSquidKiotaClient> mediaService = new AtomicReference<>();
    private final AtomicReference<OniKiotaClient> conceptService = new AtomicReference<>();
    private final AtomicReference<ImageArchiveService> imageArchiveService = new AtomicReference<>();
    private final AtomicReference<PreferencesService> preferencesService = new AtomicReference<>();

    private final String NAME_ANNOSAURUS = "annosaurus";
    private final String NAME_VAMPIRE_SQUID = "vampire-squid";
    private final String NAME_ONI = "oni";
    private final String NAME_PANOPTES = "panoptes";

    /** Used when no timeout is given. The OS connect timeout alone can be over a minute. */
    public static final Duration DEFAULT_RAZIEL_TIMEOUT = Duration.ofSeconds(10);

    private final Duration razielTimeout;

    /** Raziel is only contacted once per builder, even if it fails. See loadConfigurations. */
    private boolean configurationAttempted = false;
    private ServiceConfigurationException configurationError;

    public ServiceBuilder(boolean load) {
        this(load, DEFAULT_RAZIEL_TIMEOUT);
    }

    /**
     * @param load true to load the service endpoints from the configuration server (Raziel),
     *             false to read them from reference.conf
     * @param razielTimeout How long to wait for each request to the configuration server
     */
    public ServiceBuilder(boolean load, Duration razielTimeout) {
        this.load = load;
        this.razielTimeout = razielTimeout;
    }

    /**
     * @return Why the service endpoints could not be loaded from the configuration server.
     *      Empty if they were loaded (or have not been requested yet).
     */
    public synchronized Optional<ServiceConfigurationException> getConfigurationError() {
        return Optional.ofNullable(configurationError);
    }

    private synchronized void loadConfigurations() {
        if (load) {
            // Only try once. Every get*Service method calls this, so retrying on failure meant
            // one full connection timeout per service before the app could start.
            if (configurationAttempted) {
                return;
            }
            configurationAttempted = true;

            // raziel.txt is the text from Raziel's /config/auth/encode. We swap it for an access token.
            var loginFile = Raziel.LoginFile.load();
            if (loginFile.isEmpty()) {
                log.atWarn().log("No Raziel login file was found. The configuration server has not been set up.");
                configurationError = new ServiceConfigurationException(
                        ServiceConfigurationException.Reason.NOT_CONFIGURED,
                        null,
                        "No configuration server has been set up",
                        null);
                return;
            }

            var file = loginFile.get();
            try {
                log.atInfo().log("Connecting to Raziel at " + file.url());
                var urlString = ServiceBuilder.adaptUrl(file.url().toString());
                var uri = URI.create(urlString);
                var client = new RazielKiotaClient(uri);
                var timeoutMillis = razielTimeout.toMillis();
                var bearerAuth = client.login(file.content())
                        .orTimeout(timeoutMillis, TimeUnit.MILLISECONDS)
                        .join();
                var services = client.endpoints(bearerAuth.accessToken())
                        .orTimeout(timeoutMillis, TimeUnit.MILLISECONDS)
                        .join();
                endpoints.addAll(services);
                if (services.isEmpty()) {
                    log.atWarn().log("Raziel at " + file.url() + " did not return any service endpoints");
                    configurationError = new ServiceConfigurationException(
                            ServiceConfigurationException.Reason.NO_SERVICES,
                            file.url(),
                            "The configuration server did not return any services",
                            null);
                }
            }
            catch (Exception e) {
                configurationError = ServiceConfigurationException.from(file.url(), e);
                log.atError().withCause(e).log("Failed to load Raziel connection parameters (" +
                        configurationError.getReason() + ")");
            }
        }
        else if (endpoints.isEmpty()){
            // Read from config
            log.atInfo().log("Loading service endpoints from configuration (reference.conf)");
            try {
                var names = Map.of(
                        "annotation.service", NAME_ANNOSAURUS,
                        "media.service", NAME_VAMPIRE_SQUID,
                        "concept.service", NAME_ONI,
                        "panoptes.service", NAME_PANOPTES
                );

                var config = ConfigFactory.load();
                names.forEach((configName, serviceName) -> {
                    var uri = config.getString(configName + ".url");
                    var timeout = config.getDuration(configName + ".timeout");
                    var secret = config.getString(configName + ".client.secret");
                    var endpoint = new EndpointConfig(serviceName, uri, timeout.toMillis(), secret);
                    endpoints.add(endpoint);
                });

            }
            catch (Exception e) {
                log.atError().withCause(e).log("Failed to load configuration parameters");
            }
        }
    }

    public synchronized AnnotationService getAnnotationService() {
        if (annotationService.get() == null) {
            loadConfigurations();
            var config = endpoints.stream()
                    .filter(e -> e.name().equals(NAME_ANNOSAURUS))
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
                    .filter(e -> e.name().equals(NAME_VAMPIRE_SQUID))
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
                    .filter(e -> e.name().equals(NAME_ONI))
                    .map(this::adaptEndpointConfigForKiota)
                    .findFirst();
            if (config.isPresent()) {
                var endpoint = config.get();
                var uri = URI.create(endpoint.url());
                var client = new OniKiotaClient(uri, endpoint.secret());
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
                    .filter(e -> e.name().equals(NAME_PANOPTES))
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

    public synchronized PreferencesService getPreferencesService() {

        if (preferencesService.get() == null) {
            loadConfigurations();
            var config = endpoints.stream()
                    .filter(e -> e.name().equals(NAME_ONI))
                    .map(this::adaptEndpointConfigForKiota)
                    .findFirst();
            if (config.isPresent()) {
                var endpoint = config.get();
                var uri = URI.create(endpoint.url());
                var client = new OniKiotaClient(uri, endpoint.secret());
                // OniKiotaClient implements PreferencesService, wrap it with CachedPreferencesService
                var service = new CachedPreferencesService(client);
                preferencesService.set(service);
                return service;
            } else {
                log.atWarn().log("No Oni endpoint found for preferences service");
                return null;
            }
        }
        return preferencesService.get();
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
