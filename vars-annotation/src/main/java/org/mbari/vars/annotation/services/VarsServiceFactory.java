package org.mbari.vars.annotation.services;

import com.typesafe.config.Config;
import org.mbari.vars.annosaurus.sdk.r1.NoopAnnotationService;
import org.mbari.vars.annotation.services.noop.NoopImageArchiveService;
import org.mbari.vars.annotation.services.noop.NoopPreferencesFactory;
import org.mbari.vars.annotation.services.noop.NoopPreferencesService;
import org.mbari.vars.annotation.services.noop.NoopUserService;
import org.mbari.vars.annotation.services.oni.ModifyingConceptService;
import org.mbari.vars.annotation.services.oni.PreferencesFactory;
import org.mbari.vars.annotation.services.oni.WebPreferencesFactory;
import org.mbari.vars.oni.sdk.r1.*;
import org.mbari.vars.vampiresquid.sdk.r1.NoopMediaService;

import java.time.Duration;
import java.util.List;

public class VarsServiceFactory implements ServiceFactory {

    private static final String CONFIG_KEY = "concept.service.template.filters";
    private final Config config;

    /**
     * This is the root config note. Typically loaded via Initializer.getConfig(). Should contain the configuration key
     * `concept.service.template.filters` (Something like `concept.service.template.filters = ["^dsg.*"`)
     * @param config A config node.
     */
    public VarsServiceFactory(Config config) {
        this.config = config;
    }


    @Override
    public Services newServices() {

        var defaultTimeout = Duration.ofSeconds(20);
        // Fetch info from Raziel and build services
        var serviceBuilder = new ServiceBuilder(true);
        var annosaurusClient = serviceBuilder.getAnnotationService();
        var oniClient = serviceBuilder.getConceptService();
        var vampireSquidClient = serviceBuilder.getMediaService();
        var panoptesClient = serviceBuilder.getImageArchiveService();

        // The concept service has a lot of parts. We cache data and filter out unwanted link templates
        List<String> regex = config.getStringList(CONFIG_KEY);
        ModifyingConceptService modifyingConceptService = new ModifyingConceptService(oniClient, regex);
        CachedConceptService cachedConceptService = new CachedConceptService(modifyingConceptService);

        // Build preferences classes from oniClient
        PreferencesService cachedPreferencesService = new CachedPreferencesService((PreferencesService) oniClient);
        PreferencesFactory preferencesFactory = new WebPreferencesFactory(cachedPreferencesService, defaultTimeout.toMillis());

        return new Services(annosaurusClient,
                cachedConceptService,
                panoptesClient,
                vampireSquidClient,
                (UserService) oniClient,
                cachedPreferencesService,
                preferencesFactory);

    }

    public static Services noop() {
        return new Services(
                new NoopAnnotationService(),
                new NoopConceptService(),
                new NoopImageArchiveService(),
                new NoopMediaService(),
                new NoopUserService(),
                new NoopPreferencesService(),
                new NoopPreferencesFactory()
        );
    }
}
