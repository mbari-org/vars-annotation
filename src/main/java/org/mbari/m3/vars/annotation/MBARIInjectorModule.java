package org.mbari.m3.vars.annotation;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.mbari.m3.vars.annotation.model.Authorization;
import org.mbari.m3.vars.annotation.services.*;
import org.mbari.m3.vars.annotation.services.annosaurus.v1.AnnoService;
import org.mbari.m3.vars.annotation.services.annosaurus.v1.AnnoWebServiceFactory;
import org.mbari.m3.vars.annotation.services.vampiresquid.v1.VamService;
import org.mbari.m3.vars.annotation.services.vampiresquid.v1.VamWebServiceFactory;
import org.mbari.m3.vars.annotation.services.varskbserver.v1.KBConceptService;
import org.mbari.m3.vars.annotation.services.varskbserver.v1.KBWebServiceFactory;
import org.mbari.m3.vars.annotation.services.varsuserserver.v1.KBMiscServiceFactory;
import org.mbari.m3.vars.annotation.services.varsuserserver.v1.KBPrefService;
import org.mbari.m3.vars.annotation.services.varsuserserver.v1.KBUserService;
import org.mbari.m3.vars.annotation.util.PreferencesFactory;
import org.mbari.m3.vars.annotation.util.WebPreferencesFactory;

import java.time.Duration;

/**
 * @author Brian Schlining
 * @since 2017-05-11T16:00:00
 */
public class MBARIInjectorModule implements Module {

    private final Config config;

    public MBARIInjectorModule() {
        this.config = ConfigFactory.load();
    }

    @Override
    public void configure(Binder binder) {
        configureAnnotationService(binder);
        configureMediaService(binder);
        configureConceptService(binder);
        configurePrefsServices(binder);
    }

    private void configureAnnotationService(Binder binder) {
        String endpoint = config.getString("annotation.service.url");
        String clientSecret = config.getString("annotation.service.client.secret");
        AnnoWebServiceFactory factory = new AnnoWebServiceFactory(endpoint);
        AuthService authService = new BasicJWTAuthService(factory,
                new Authorization("APIKEY", clientSecret));
        binder.bind(String.class)
                .annotatedWith(Names.named("ANNO_ENDPOINT"))
                .toInstance(endpoint);
        binder.bind(AuthService.class)
                .annotatedWith(Names.named("ANNO_AUTH"))
                .toInstance(authService);
        binder.bind(AnnoWebServiceFactory.class).toInstance(factory);
        binder.bind(AnnotationService.class).to(AnnoService.class);
    }

    private void configureMediaService(Binder binder) {
        String endpoint = config.getString("media.service.url");
        String clientSecret = config.getString("media.service.client.secret");
        VamWebServiceFactory factory = new VamWebServiceFactory(endpoint);
        AuthService authService = new BasicJWTAuthService(factory,
                new Authorization("APIKEY", clientSecret));
        binder.bind(String.class)
                .annotatedWith(Names.named("MEDIA_ENDPOINT"))
                .toInstance(endpoint);
        binder.bind(AuthService.class)
                .annotatedWith(Names.named("MEDIA_AUTH"))
                .toInstance(authService);
        binder.bind(VamWebServiceFactory.class).toInstance(factory);
        binder.bind(MediaService.class).to(VamService.class);
    }

    private void configureConceptService(Binder binder) {
        String endpoint = config.getString("concept.service.url");
        KBWebServiceFactory factory = new KBWebServiceFactory(endpoint);
        binder.bind(String.class)
                .annotatedWith(Names.named("CONCEPT_ENDPOINT"))
                .toInstance(endpoint);
        binder.bind(KBWebServiceFactory.class).toInstance(factory);
        binder.bind(ConceptService.class).to(KBConceptService.class);
    }
    private void configurePrefsServices(Binder binder) {
        String endpoint = config.getString("preferences.service.url");
        String clientSecret = config.getString("preferences.service.client.secret");
        Duration prefsTimeout = config.getDuration("preferences.service.timeout");
        KBMiscServiceFactory factory = new KBMiscServiceFactory(endpoint);
        AuthService authService = new BasicJWTAuthService(factory,
                new Authorization("APIKEY", clientSecret));
        binder.bind(String.class)
                .annotatedWith(Names.named("MISC_ENDPOINT"))
                .toInstance(endpoint);
        binder.bind(Long.class)
                .annotatedWith(Names.named("PREFS_TIMEOUT"))
                .toInstance(prefsTimeout.toMillis());
        binder.bind(AuthService.class)
                .annotatedWith(Names.named("PREFS_AUTH"))
                .toInstance(authService);
        binder.bind(AuthService.class)
                .annotatedWith(Names.named("USERS_AUTH"))
                .toInstance(authService);
        binder.bind(KBMiscServiceFactory.class).toInstance(factory);
        binder.bind(PreferencesService.class).to(KBPrefService.class);
        binder.bind(UserService.class).to(KBUserService.class);
        binder.bind(PreferencesFactory.class).to(WebPreferencesFactory.class);
    }



}
