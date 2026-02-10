package org.mbari.vars.annotation;

import com.typesafe.config.Config;
import org.mbari.vars.annotation.etc.jdk.Loggers;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

/**
 * We are avoiding scattering config strings through the codebase. The AppConfig
 * provides fixed endpoints for looking up config values
 * @author Brian Schlining
 * @since 2019-05-14T14:47:00
 */
public class AppConfig  {

    private final Loggers log = new Loggers(getClass());

    private final Config config;

    public enum PagingStyle {
        PARALLEL,
        SEQUENTIAL
    }

    public record AnnotationService(Duration timeout, int pageSize, PagingStyle paging, int pageCount) {}
    public record ConceptService(Duration timeout, List<String> templateFilters) {}
    public record Localization(int port, String topic) {}
    public record Service(Duration timeout) {}
    public record Sharktopoda(int controlPort, int framegrabPort) {}

    public record Sample(
        String associationComment,
        String associationEquipment,
        String associationPopulation,
        String associationReference,
        String defaultConcept,
        List<String> linkNames
    ) {}

    public record Annotation(
        Sample sample,
        String identityReference,
        String uponLinkname,
        String uponRoot,
        List<String> detailsCache
    ) {}

    public record Defaults(
        String cameraId,
        String group,
        String activity
    ) {}

    public record App(
        Annotation annotation,
        Sample sample,
        Defaults defaults,
        String imageCopyrightOwner
    ) {}

    public record Root(
        AnnotationService annotationService,
        App app,
        ConceptService conceptService,
        Localization defaultsLocalization,
        Localization outgoingLocalization,
        Service mediaService,
        Service panoptesService,
        Service preferencesService,
        Sharktopoda sharktopoda
    ) {}

    private final Root applicationConfig;

    public AppConfig(Config config) {
        this.config = config;
        this.applicationConfig = loadApplication();
    }

    private Root loadApplication() {
        return new Root(
                loadAnnotationService(),
                loadBaseConfig(),
                loadConceptService(),
                loadIncomingLocalization(),
                loadOutgoingLocalization(),
                loadMediaService(),
                loadPanoptesService(),
                loadPreferencesService(),
                loadSharktopoda()
        );
    }

    private AnnotationService loadAnnotationService() {
        return new AnnotationService(
                read("annotation.service.timeout", config::getDuration, Duration.ofSeconds(30)),
                read("annotation.service.page.size", config::getInt, 1000),
                loadPagingStyle(),
                read("annotation.service.page.count", config::getInt, 2)
        );
    }

    private PagingStyle loadPagingStyle() {
        return read("annotation.service.paging", (s) -> config.getString(s).startsWith("par") ?
                PagingStyle.PARALLEL : PagingStyle.SEQUENTIAL, PagingStyle.SEQUENTIAL);
    }

    private Sample loadSample() {
        return new Sample(
                read("app.annotation.sample.association.comment", config::getString, "comment"),
                read("app.annotation.sample.association.equipment", config::getString, "sampled-by"),
                read("app.annotation.sample.association.population", config::getString, "population-quantity"),
                read("app.annotation.sample.association.reference", config::getString, "sample-reference"),
                read("app.annotation.sample.default.concept", config::getString, "equipment"),
                read("app.annotation.sample.linknames", config::getStringList, List.of("sample-reference", "sampled-by"))
        );
    }

    private Annotation loadAnnotationConfig() {
        return new Annotation(
                loadSample(),
                read("app.annotation.identity.reference", config::getString, "identity-reference"),
                read("app.annotation.upon.linkname", config::getString, "upon"),
                read("app.annotation.upon.root", config::getString, "physical object"),
                read("app.annotation.details.cache", config::getStringList, List.of("marine organism", "object", "physical object"))
        );
    }

    private Defaults loadDefaults() {
        return new Defaults(
                read("app.defaults.cameraid", config::getString, ""),
                read("app.defaults.group", config::getString, "ROV"),
                read("app.defaults.activity", config::getString, "descend")
        );
    }

    private App loadBaseConfig() {
        return new App(
                loadAnnotationConfig(),
                loadSample(),
                loadDefaults(),
                read("app.image.copyright.owner", config::getString, "")
        );
    }

    private ConceptService loadConceptService() {
        return new ConceptService(
                read("concept.service.timeout", config::getDuration, Duration.ofSeconds(5)),
                read("concept.service.template.filters", config::getStringList, List.of("^dsg.*"))
        );
    }

    private Localization loadIncomingLocalization() {
        return new Localization(
                read("localization.defaults.incoming.port", config::getInt, 5561),
                read("localization.defaults.incoming.topic", config::getString, "localization")
        );
    }

    private Localization loadOutgoingLocalization() {
        return new Localization(
                read("localization.defaults.outgoing.port", config::getInt, 5562),
                read("localization.defaults.outgoing.topic", config::getString, "localization")
        );
    }

    private Service loadMediaService() {
        return new Service(
                read("media.service.timeout", config::getDuration, Duration.ofSeconds(10))
        );
    }

    private Service loadPanoptesService() {
        return new Service(
                read("panoptes.service.timeout", config::getDuration, Duration.ofSeconds(60))
        );
    }

    private Service loadPreferencesService() {
        return new Service(
                read("preferences.service.timeout", config::getDuration, Duration.ofSeconds(5))
        );
    }

    private Sharktopoda loadSharktopoda() {
        return new Sharktopoda(
                read("sharktopoda.defaults.control.port", config::getInt, 8800),
                read("sharktopoda.defaults.framegrab.port", config::getInt, 5000)
        );
    }

    public Root getRoot() {
        return applicationConfig;
    }

//    public BaseConfig getBaseConfig() {
//        return applicationConfig.app();
//    }

    // --- Delegating getters for backward compatibility ---

    public int getSharktopodaDefaultsControlPort() {
        return applicationConfig.sharktopoda().controlPort();
    }

    public int getSharktopodaDefaultsFramegrabPort() {
        return applicationConfig.sharktopoda().framegrabPort();
    }

    public int getLocalizationDefaultsIncomingPort() {
        return applicationConfig.defaultsLocalization().port();
    }

    public String getLocalizationDefaultsIncomingTopic() {
        return applicationConfig.defaultsLocalization().topic();
    }

    public int getLocalizationDefaultsOutgoingPort() {
        return applicationConfig.outgoingLocalization().port();
    }

    public String getLocalizationDefaultsOutgoingTopic() {
        return applicationConfig.outgoingLocalization().topic();
    }

    public List<String> getAppAnnotationSampleLinknames() {
        return applicationConfig.app().sample().linkNames();
    }

    public PagingStyle getAnnotationServiceV1Paging() {
        return applicationConfig.annotationService().paging();
    }

    public String getAppDefaultsActivity() {
        return applicationConfig.app().defaults().activity();
    }

    public String getAppDefaultsCameraId() {
        return applicationConfig.app().defaults().cameraId();
    }

    public String getAppDefaultsGroup() {
        return applicationConfig.app().defaults().group();
    }

    public String getAppAnnotationIdentityReference() {
        return applicationConfig.app().annotation().identityReference();
    }

    public String getAppAnnotationSampleAssociationComment() {
        return applicationConfig.app().sample().associationComment();
    }

    public String getAppAnnotationSampleAssociationEquipment() {
        return applicationConfig.app().sample().associationEquipment();
    }

    public String getAppAnnotationSampleAssociationReference() {
        return applicationConfig.app().sample().associationReference();
    }

    public String getAppAnnotationSampleDefaultConcept() {
        return applicationConfig.app().sample().defaultConcept();
    }

    public String getAppAnnotationUpon() {
        return applicationConfig.app().annotation().uponLinkname();
    }

    public String getAppAnnotationUponRoot() {
        return applicationConfig.app().annotation().uponRoot();
    }

    public String getAppImageCopyrightOwner() {
        return applicationConfig.app().imageCopyrightOwner();
    }

    public <T> T read(String path, Function<String, T> fn, T defaultValue) {
        try {
            return fn.apply(path);
        }
        catch (Exception e) {
            log.atWarn().log("Unable to find a config value at path " +  path);
            return defaultValue;
        }
    }

//    public URL readUrl(String path) {
//        String url = null;
//        try {
//            url = config.getString(path);
//            return URI.create(url).toURL();
//        }
//        catch (MalformedURLException e) {
//            log.warn("The URL {} defined in the config at {} is malformed", url, path);
//            return defaultUrl;
//        }
//        catch (Exception e) {
//            log.warn("Unable to find a config value at path {}", path);
//            return defaultUrl;
//        }
//    }

}
