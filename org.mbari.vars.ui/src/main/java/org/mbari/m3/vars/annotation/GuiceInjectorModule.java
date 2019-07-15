package org.mbari.m3.vars.annotation;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.typesafe.config.Config;
import org.mbari.vars.services.model.Authorization;
import org.mbari.vars.services.*;
import org.mbari.vars.services.impl.annosaurus.v1.AnnoService;
import org.mbari.vars.services.impl.annosaurus.v1.AnnoWebServiceFactory;
import org.mbari.vars.services.impl.annosaurus.v2.AnnoWebServiceFactoryV2;
import org.mbari.vars.services.impl.panoptes.v1.PanoptesService;
import org.mbari.vars.services.impl.panoptes.v1.PanoptesWebServiceFactory;
import org.mbari.vars.services.impl.vampiresquid.v1.VamService;
import org.mbari.vars.services.impl.vampiresquid.v1.VamWebServiceFactory;
import org.mbari.vars.services.impl.varskbserver.v1.KBConceptService;
import org.mbari.vars.services.impl.varskbserver.v1.KBWebServiceFactory;
import org.mbari.vars.services.impl.varsuserserver.v1.*;
import org.mbari.vars.services.util.PreferencesFactory;
import org.mbari.vars.services.util.WebPreferencesFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * @author Brian Schlining
 * @since 2017-05-11T16:00:00
 */
public class GuiceInjectorModule implements Module {

    private final Config config;
    private final AppConfig appConfig;
    private final Executor defaultExecutor = new ForkJoinPool();

    public GuiceInjectorModule() {
        this.config = Initializer.getConfig();
        this.appConfig = new AppConfig(config);
    }

    @Override
    public void configure(Binder binder) {
        configureAnnotationService(binder);
        configureAnnotationV2Service(binder);
        configureMediaService(binder);
        configureConceptService(binder);
        configurePrefsServices(binder);
        configureUserServices(binder);
        configurePanoptes(binder);
    }

    private void configureAnnotationService(Binder binder) {
        AppConfig.ServiceParams params = appConfig.getAnnotationServiceParamsV1();
        AnnoWebServiceFactory factory = new AnnoWebServiceFactory(params.getEndpoint(), params.getTimeout());
        AuthService authService = new BasicJWTAuthService(factory,
                new Authorization("APIKEY", params.getClientSecret()));
        binder.bind(String.class)
                .annotatedWith(Names.named("ANNO_ENDPOINT"))
                .toInstance(params.getEndpoint());
        binder.bind(AuthService.class)
                .annotatedWith(Names.named("ANNO_AUTH"))
                .toInstance(authService);
        binder.bind(AnnoWebServiceFactory.class).toInstance(factory);
        binder.bind(AnnotationService.class).to(AnnoService.class);
    }

    private void configureAnnotationV2Service(Binder binder) {
        AppConfig.ServiceParams params = appConfig.getAnnotationServiceParamsV2();
        AnnoWebServiceFactoryV2 factory = new AnnoWebServiceFactoryV2(params.getEndpoint(),
                params.getTimeout());
//        AuthService authService = new BasicJWTAuthService(factory,
//                new Authorization("APIKEY", clientSecret));
        binder.bind(String.class)
                .annotatedWith(Names.named("ANNO_ENDPOINT_V2"))
                .toInstance(params.getEndpoint());
//        binder.bind(AuthService.class)
//                .annotatedWith(Names.named("ANNO_AUTH"))
//                .toInstance(authService);
        binder.bind(AnnoWebServiceFactoryV2.class).toInstance(factory);
//        binder.bind(AnnoServiceV2.class).to(AnnoServiceV2.class);
    }

    private void configureMediaService(Binder binder) {
        AppConfig.ServiceParams params = appConfig.getMediaServiceParamsV1();
        VamWebServiceFactory factory = new VamWebServiceFactory(params.getEndpoint(), params.getTimeout());
        AuthService authService = new BasicJWTAuthService(factory,
                new Authorization("APIKEY", params.getClientSecret()));
        binder.bind(String.class)
                .annotatedWith(Names.named("MEDIA_ENDPOINT"))
                .toInstance(params.getEndpoint());
        binder.bind(AuthService.class)
                .annotatedWith(Names.named("MEDIA_AUTH"))
                .toInstance(authService);
        binder.bind(VamWebServiceFactory.class).toInstance(factory);
        binder.bind(MediaService.class).to(VamService.class);
    }

    private void configureConceptService(Binder binder) {
        AppConfig.ServiceParams params = appConfig.getConceptServiceParamsV1();
        KBWebServiceFactory factory = new KBWebServiceFactory(params.getEndpoint(),
                params.getTimeout(), defaultExecutor);
        KBConceptService service = new KBConceptService(factory);
        // --- Create a service that munges the data from the service for a better UI experience.
        ModifyingConceptService modService = new ModifyingConceptService(service, config);
        // --- Using a local cache
        CachedConceptService cachedService = new CachedConceptService(modService);
        //List<String> cachedConceptTemplates = config.getStringList("app.annotation.details.cache");
        //cachedService.prefetch(cachedConceptTemplates);
        binder.bind(String.class)
                .annotatedWith(Names.named("CONCEPT_ENDPOINT"))
                .toInstance(params.getEndpoint());
        binder.bind(KBWebServiceFactory.class).toInstance(factory);
        binder.bind(ConceptService.class).toInstance(cachedService);
        //binder.bind(ConceptService.class).toInstance(service);
    }

    private void configurePrefsServices(Binder binder) {
        AppConfig.ServiceParams params = appConfig.getPreferencesServiceParamsV1();
        PrefWebServiceFactory factory = new PrefWebServiceFactory(params.getEndpoint(), params.getTimeout());
        RetrofitServiceFactory authFactory = new BasicJWTAuthServiceFactorySC(params.getEndpoint(), params.getTimeout());
        AuthService authService = new BasicJWTAuthService(authFactory,
                new Authorization("APIKEY", params.getClientSecret()));
        KBPrefService preferencesService = new KBPrefService(factory, authService);
        binder.bind(Long.class)
                .annotatedWith(Names.named("PREFS_TIMEOUT"))
                .toInstance(params.getTimeout().toMillis());
        binder.bind(PreferencesService.class).toInstance(preferencesService);
        binder.bind(KBPrefService.class).toInstance(preferencesService);
        binder.bind(PreferencesFactory.class).to(WebPreferencesFactory.class);
    }

    private void configureUserServices(Binder binder) {
        AppConfig.ServiceParams params = appConfig.getAccountsServiceParamsV1();
        UserWebServiceFactory factory = new UserWebServiceFactory(params.getEndpoint(),
                params.getTimeout());
        RetrofitServiceFactory authFactory = new BasicJWTAuthServiceFactorySC(params.getEndpoint(),
                params.getTimeout());
        AuthService authService = new BasicJWTAuthService(authFactory,
                new Authorization("APIKEY", params.getClientSecret()));
        KBUserService userService = new KBUserService(factory, authService);
        binder.bind(Long.class)
                .annotatedWith(Names.named("ACCOUNTS_TIMEOUT"))
                .toInstance(params.getTimeout().toMillis());
        binder.bind(UserService.class).toInstance(userService);
        binder.bind(KBUserService.class).toInstance(userService);
    }

    private void configurePanoptes(Binder binder) {
        AppConfig.ServiceParams params = appConfig.getPanoptesServiceParamsV1();
        PanoptesWebServiceFactory factory = new PanoptesWebServiceFactory(params.getEndpoint(),
                params.getTimeout());
        RetrofitServiceFactory authFactory = new BasicJWTAuthServiceFactorySC(params.getEndpoint(),
                params.getTimeout());
        AuthService authService = new BasicJWTAuthService(authFactory,
                new Authorization("APIKEY", params.getClientSecret()));
        binder.bind(String.class)
                .annotatedWith(Names.named("PANOPTES_ENDPOINT"))
                .toInstance(params.getEndpoint());
        binder.bind(AuthService.class)
                .annotatedWith(Names.named("PANOPTES_AUTH"))
                .toInstance(authService);
        PanoptesService service = new PanoptesService(factory, authService);
        binder.bind(Long.class)
                .annotatedWith(Names.named("PANOPTES_TIMEOUT"))
                .toInstance(params.getTimeout().toMillis());
        binder.bind(PanoptesWebServiceFactory.class).toInstance(factory);
        binder.bind(ImageArchiveService.class).toInstance(service);

    }

}
