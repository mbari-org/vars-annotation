package org.mbari.vars.services;


import com.typesafe.config.Config;
import org.mbari.vars.services.impl.annosaurus.v1.AnnoService;
import org.mbari.vars.services.impl.annosaurus.v1.AnnoWebServiceFactory;
import org.mbari.vars.services.impl.annosaurus.v2.AnnoServiceV2;
import org.mbari.vars.services.impl.annosaurus.v2.AnnoWebServiceFactoryV2;
import org.mbari.vars.services.impl.panoptes.v1.PanoptesService;
import org.mbari.vars.services.impl.panoptes.v1.PanoptesWebServiceFactory;
import org.mbari.vars.services.impl.vampiresquid.v1.VamService;
import org.mbari.vars.services.impl.vampiresquid.v1.VamWebServiceFactory;
import org.mbari.vars.services.impl.varskbserver.v1.KBConceptService;
import org.mbari.vars.services.impl.varskbserver.v1.KBWebServiceFactory;
import org.mbari.vars.services.impl.varsuserserver.v1.KBPrefService;
import org.mbari.vars.services.impl.varsuserserver.v1.KBUserService;
import org.mbari.vars.services.impl.varsuserserver.v1.PrefWebServiceFactory;
import org.mbari.vars.services.impl.varsuserserver.v1.UserWebServiceFactory;
import org.mbari.vars.services.model.Authorization;
import org.mbari.vars.services.util.PreferencesFactory;
import org.mbari.vars.services.util.WebPreferencesFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

public class ServicesBuilder {

    private final Config config;
    private final ServiceConfig appConfig;
    private final Executor defaultExecutor = new ForkJoinPool();

    private static class Prefs {
        final PreferencesFactory preferencesFactory;
        final PreferencesService preferencesService;

        public Prefs(PreferencesFactory preferencesFactory, PreferencesService preferencesService) {
            this.preferencesFactory = preferencesFactory;
            this.preferencesService = preferencesService;
        }

        public PreferencesFactory getPreferencesFactory() {
            return preferencesFactory;
        }

        public PreferencesService getPreferencesService() {
            return preferencesService;
        }
    }

    private ServicesBuilder(Config config) {
        this.config = config;
        this.appConfig = new ServiceConfig(config);
    }

    public static Services build(Config config) {
        return new ServicesBuilder(config).build();
    }

    private Services build() {
        Prefs prefs = buildPrefs();
        return new Services(buildAnnotationService(),
                buildAnnotationV2Service(),
                buildConceptService(),
                buildImageArchiveService(),
                buildMediaService(),
                buildUserService(),
                prefs.getPreferencesService(),
                prefs.getPreferencesFactory());
    }

    private AnnoService buildAnnotationService() {
        ServiceConfig.ServiceParams params = appConfig.getAnnotationServiceParamsV1();
        AnnoWebServiceFactory factory = new AnnoWebServiceFactory(params.getEndpoint(), params.getTimeout());
        AuthService authService = new BasicJWTAuthService(factory,
                new Authorization("APIKEY", params.getClientSecret()));
        AnnoWebServiceFactory serviceFactory = new AnnoWebServiceFactory(params.getEndpoint(),
                params.getTimeout());
        return new AnnoService(serviceFactory, authService);
    }

    private AnnoServiceV2 buildAnnotationV2Service() {
        ServiceConfig.ServiceParams params = appConfig.getAnnotationServiceParamsV2();
        AnnoWebServiceFactoryV2 factory = new AnnoWebServiceFactoryV2(params.getEndpoint(),
                params.getTimeout());
        AuthService authService = new BasicJWTAuthService(factory,
                new Authorization("APIKEY", params.getClientSecret()));
        return new AnnoServiceV2(factory, authService);
    }

    private MediaService buildMediaService() {
        ServiceConfig.ServiceParams params = appConfig.getMediaServiceParamsV1();
        VamWebServiceFactory factory = new VamWebServiceFactory(params.getEndpoint(), params.getTimeout());
        AuthService authService = new BasicJWTAuthService(factory,
                new Authorization("APIKEY", params.getClientSecret()));
        return new VamService(factory, authService);
    }

    private ConceptService buildConceptService() {
        ServiceConfig.ServiceParams params = appConfig.getConceptServiceParamsV1();
        KBWebServiceFactory factory = new KBWebServiceFactory(params.getEndpoint(),
                params.getTimeout(), defaultExecutor);
        KBConceptService service = new KBConceptService(factory);
        // --- Create a service that munges the data from the service for a better UI experience.
        ModifyingConceptService modService = new ModifyingConceptService(service, config);
        // --- Using a local cache
        return new CachedConceptService(modService);
    }

    private Prefs buildPrefs() {
        ServiceConfig.ServiceParams params = appConfig.getPreferencesServiceParamsV1();
        PrefWebServiceFactory factory = new PrefWebServiceFactory(params.getEndpoint(), params.getTimeout());
        RetrofitServiceFactory authFactory = new BasicJWTAuthServiceFactorySC(params.getEndpoint(), params.getTimeout());
        AuthService authService = new BasicJWTAuthService(authFactory,
                new Authorization("APIKEY", params.getClientSecret()));
        KBPrefService prefService = new KBPrefService(factory, authService);
        PreferencesFactory prefsFactory = new WebPreferencesFactory(prefService, params.getTimeout().toMillis());
        return new Prefs(prefsFactory, prefService);
    }

    private UserService buildUserService() {
        ServiceConfig.ServiceParams params = appConfig.getAccountsServiceParamsV1();
        UserWebServiceFactory factory = new UserWebServiceFactory(params.getEndpoint(),
                params.getTimeout());
        RetrofitServiceFactory authFactory = new BasicJWTAuthServiceFactorySC(params.getEndpoint(),
                params.getTimeout());
        AuthService authService = new BasicJWTAuthService(authFactory,
                new Authorization("APIKEY", params.getClientSecret()));
        return new KBUserService(factory, authService);
    }

    private ImageArchiveService buildImageArchiveService() {
        ServiceConfig.ServiceParams params = appConfig.getPanoptesServiceParamsV1();
        PanoptesWebServiceFactory factory = new PanoptesWebServiceFactory(params.getEndpoint(),
                params.getTimeout());
        RetrofitServiceFactory authFactory = new BasicJWTAuthServiceFactorySC(params.getEndpoint(),
                params.getTimeout());
        AuthService authService = new BasicJWTAuthService(authFactory,
                new Authorization("APIKEY", params.getClientSecret()));
        return new PanoptesService(factory, authService);
    }



}
