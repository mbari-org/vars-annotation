package org.mbari.vars.services;

import com.typesafe.config.Config;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.mbari.vars.services.impl.annosaurus.v1.AnnoService;
import org.mbari.vars.services.impl.annosaurus.v1.AnnoWebServiceFactory;
import org.mbari.vars.services.impl.annosaurus.v1.AnnosaurusHttpClient;
import org.mbari.vars.services.impl.panoptes.v1.PanoptesService;
import org.mbari.vars.services.impl.panoptes.v1.PanoptesWebServiceFactory;
import org.mbari.vars.services.impl.vampiresquid.v1.VamService;
import org.mbari.vars.services.impl.vampiresquid.v1.VamWebServiceFactory;
import org.mbari.vars.services.impl.varskbserver.v1.KBConceptService;
import org.mbari.vars.services.impl.varskbserver.v1.KBWebServiceFactory;
import org.mbari.vars.services.impl.varsuserserver.v1.*;
import org.mbari.vars.services.model.Authorization;
import org.mbari.vars.services.model.EndpointConfig;
import org.mbari.vars.services.noop.*;
import org.mbari.vars.services.util.PreferencesFactory;
import org.mbari.vars.services.util.WebPreferencesFactory;

public class ServicesBuilder {

  private final Config config;
  private final ServiceConfig appConfig;
  private final Executor defaultExecutor = new ForkJoinPool();

  public static class Prefs {
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

  public ServicesBuilder(Config config) {
    this.config = config;
    this.appConfig = new ServiceConfig(config);
  }

  public static Services build(Config config) {
    return new ServicesBuilder(config).build();
  }

  private Services build() {
    Prefs prefs = buildPrefs();
    return new Services(
        buildAnnotationService(),
        buildConceptService(),
        buildImageArchiveService(),
        buildMediaService(),
        buildUserService(),
        prefs.getPreferencesService(),
        prefs.getPreferencesFactory());
  }

  public static Services noop() {
    return new Services(new NoopAnnotationService(),
            new NoopConcepService(),
            new NoopImageArchiveService(),
            new NoopMediaService(),
            new NoopUserService(),
            new NoopPreferencesService(),
            new NoopPreferencesFactory());
  }

  public static Services buildForUI(List<EndpointConfig> endpoints) {
    if (endpoints.size() < 5) {
      throw new IllegalArgumentException("Endpoints requires 5 items. Found " + endpoints.size());
    }
    var namedEndpoints = endpoints.stream()
            .collect(Collectors.toMap(EndpointConfig::getName, Function.identity()));
    var annoE = namedEndpoints.get("annosaurus");
    var kbE = Optional.ofNullable(namedEndpoints.get("vars-kb-server")).orElse(namedEndpoints.get("oni"));
    var imgE = namedEndpoints.get("panoptes");
    var mediaE = namedEndpoints.get("vampire-squid");
    var userE = Optional.ofNullable(namedEndpoints.get("vars-user-server")).orElse(namedEndpoints.get("oni"));
    var prefs = buildPrefs(userE.getUrl().toExternalForm(), userE.getTimeout(), userE.getSecret());
    var mediaService = buildMediaService(mediaE.getUrl().toExternalForm(), mediaE.getTimeout(), mediaE.getSecret());
    return new Services(
            buildAnnotationService(annoE.getUrl().toExternalForm(), annoE.getTimeout(), annoE.getSecret()),
            buildConceptService(kbE.getUrl().toExternalForm(), kbE.getTimeout(), kbE.getSecret()),
            buildImageArchiveService(imgE.getUrl().toExternalForm(), imgE.getTimeout(), imgE.getSecret()),
            new CachedMediaService(mediaService),
            buildUserService(userE.getUrl().toExternalForm(), userE.getTimeout(), userE.getSecret()),
            prefs.getPreferencesService(),
            prefs.getPreferencesFactory()
    );
  }

  public static AnnotationService buildAnnotationService(String endpoint, Duration timeout, String clientSecret) {
//    AnnoWebServiceFactory factory =
//            new AnnoWebServiceFactory(endpoint, timeout);
//    AuthService authService =
//            new BasicJWTAuthService(factory, new Authorization("APIKEY", clientSecret));
//    return new AnnoService(factory, authService);
    return new AnnosaurusHttpClient(endpoint, timeout, clientSecret);
  }

  public AnnotationService buildAnnotationService() {
    ServiceConfig.ServiceParams params = appConfig.getAnnotationServiceParamsV1();
    return buildAnnotationService(params.getEndpoint(), params.getTimeout(), params.getClientSecret());
  }

  public static MediaService buildMediaService(String endpoint, Duration timeout, String clientSecret) {
    VamWebServiceFactory factory =
            new VamWebServiceFactory(endpoint, timeout);
    AuthService authService =
            new BasicJWTAuthService(factory, new Authorization("APIKEY", clientSecret));
    return new VamService(factory, authService);
  }

  public MediaService buildMediaService() {
    ServiceConfig.ServiceParams params = appConfig.getMediaServiceParamsV1();
    return buildMediaService(params.getEndpoint(), params.getTimeout(), params.getClientSecret());
  }

  public static ConceptService buildConceptService(String endpoint, Duration timeout, String clientSecret) {
    KBWebServiceFactory factory =
            new KBWebServiceFactory(endpoint, timeout, new ForkJoinPool());
    KBConceptService service = new KBConceptService(factory);
    // --- Using a local cache
    return new CachedConceptService(service);
  }

  public ConceptService buildConceptService(String endpoint,
                             Duration timeout,
                             String clientSecret,
                             List<String> associationTemplateFilterRegex) {
    KBWebServiceFactory factory =
            new KBWebServiceFactory(endpoint, timeout, defaultExecutor);
    KBConceptService service = new KBConceptService(factory);
    // --- Create a service that munges the data from the service for a better UI experience.
    ModifyingConceptService modService = new ModifyingConceptService(service, associationTemplateFilterRegex);
    // --- Using a local cache
    return new CachedConceptService(modService);
  }

  public ConceptService buildConceptService() {
    ServiceConfig.ServiceParams params = appConfig.getConceptServiceParamsV1();
    KBWebServiceFactory factory =
        new KBWebServiceFactory(params.getEndpoint(), params.getTimeout(), defaultExecutor);
    KBConceptService service = new KBConceptService(factory);
    // --- Create a service that munges the data from the service for a better UI experience.
    ModifyingConceptService modService = new ModifyingConceptService(service, config);
    // --- Using a local cache
    return new CachedConceptService(modService);
  }

  public static Prefs buildPrefs(String endpoint, Duration timeout, String clientSecret) {
    PrefWebServiceFactory factory =
            new PrefWebServiceFactory(endpoint, timeout);
    RetrofitServiceFactory authFactory =
            new BasicJWTAuthServiceFactorySC(endpoint, timeout);
    AuthService authService =
            new BasicJWTAuthService(authFactory, new Authorization("APIKEY", clientSecret));
    KBPrefService prefService = new KBPrefService(factory, authService);
    var cachePrefService = new CachedKBPrefService(prefService);
    PreferencesFactory prefsFactory =
            new WebPreferencesFactory(cachePrefService, timeout.toMillis());
    return new Prefs(prefsFactory, cachePrefService);
  }

  public Prefs buildPrefs() {
    ServiceConfig.ServiceParams params = appConfig.getPreferencesServiceParamsV1();
    return buildPrefs(params.getEndpoint(), params.getTimeout(), params.getClientSecret());
  }

  public static UserService buildUserService(String endpoint, Duration timeout, String clientSecret) {
    UserWebServiceFactory factory =
            new UserWebServiceFactory(endpoint, timeout);
    RetrofitServiceFactory authFactory =
            new BasicJWTAuthServiceFactorySC(endpoint, timeout);
    AuthService authService =
            new BasicJWTAuthService(authFactory, new Authorization("APIKEY", clientSecret));
    return new KBUserService(factory, authService);
  }

  private UserService buildUserService() {
    ServiceConfig.ServiceParams params = appConfig.getAccountsServiceParamsV1();
    return buildUserService(params.getEndpoint(), params.getTimeout(), params.getClientSecret());
  }

  public static ImageArchiveService buildImageArchiveService(String endpoint, Duration timeout, String clientSecret) {
    PanoptesWebServiceFactory factory =
            new PanoptesWebServiceFactory(endpoint, timeout);
    RetrofitServiceFactory authFactory =
            new BasicJWTAuthServiceFactorySC(endpoint, timeout);
    AuthService authService =
            new BasicJWTAuthService(authFactory, new Authorization("APIKEY", clientSecret));
    return new PanoptesService(factory, authService);
  }

  private ImageArchiveService buildImageArchiveService() {
    ServiceConfig.ServiceParams params = appConfig.getPanoptesServiceParamsV1();
    return buildImageArchiveService(params.getEndpoint(), params.getTimeout(), params.getClientSecret());
  }
}
