package org.mbari.m3.vars.annotation;

import com.typesafe.config.Config;
import org.mbari.vars.services.ServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * @author Brian Schlining
 * @since 2019-05-14T14:47:00
 */
public class AppConfig extends ServiceConfig  {

    public enum PagingStyle {
        PARALLEL,
        SEQUENTIAL
    }

    public static class ServiceParams {
        private final String endpoint;
        private final Duration timeout;
        private final String clientSecret;

        ServiceParams(String endpoint, Duration timeout, String clientSecret) {
            this.endpoint = endpoint;
            this.timeout = timeout;
            this.clientSecret = clientSecret;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public Duration getTimeout() {
            return timeout;
        }

        public String getClientSecret() {
            return clientSecret;
        }
    }

    private final Config config;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private URL defaultUrl;
    private Duration defaultTimeout = Duration.ofSeconds(20);


    public AppConfig(Config config) {
        this.config = config;
        try {
            defaultUrl = new URL("http://localhost");
        }
        catch (MalformedURLException e) {
            log.error("Unable to create a default URL for config values");
        }
    }

    private <T> T read(String path, Function<String, T> fn, T defaultValue) {
        try {
            return fn.apply(path);
        }
        catch (Exception e) {
            log.warn("Unable to find a config value at path {}", path);
            return defaultValue;
        }
    }

    private URL readUrl(String path) {
        String url = null;
        try {
            url = config.getString(path);
            return new URL(url);
        }
        catch (MalformedURLException e) {
            log.warn("The URL {} defined in the config at {} is malformed", url, path);
            return defaultUrl;
        }
        catch (Exception e) {
            log.warn("Unable to find a config value at path {}", path);
            return defaultUrl;
        }
    }

    private ServiceParams readServiceParams(String basePath) {
        String endpoint = read(basePath + ".url", config::getString, null);
        Duration timeout = read(basePath + ".timeout", config::getDuration, defaultTimeout);
        String clientSecret = read(basePath + ".client.secret", config::getString, "");
        return new ServiceParams(endpoint, timeout, clientSecret);
    }

    public int getAnnotationServiceV1PageSize() {
        return read("annotation.service.page.size", config::getInt, 20);
    }

    public int getAnnotationsServiceV1PageCount() {
        PagingStyle pagingStyle = getAnnotationServiceV1Paging();
        return pagingStyle.equals(PagingStyle.SEQUENTIAL) ?
                1 :  read("annotation.service.page.count", config::getInt, 1);
    }

    public int getSharktopodaDefaultsControlPort() {
        return read("sharktopoda.defaults.control.port", config::getInt, 8800);
    }

    public int getSharktopodaDefaultsFramegrabPort() {
        return read("sharktopoda.defaults.framegrab.port", config::getInt, 5000);
    }

    public List<String> getAppAnnotationSampleLinknames() {
        return read("app.annotation.sample.linknames", config::getStringList, Collections.emptyList());
    }

    public PagingStyle getAnnotationServiceV1Paging() {
        return read("annotation.service.paging", (s) -> config.getString(s).startsWith("par") ?
                PagingStyle.PARALLEL : PagingStyle.SEQUENTIAL, PagingStyle.SEQUENTIAL);
    }

    public ServiceParams getAccountsServiceParamsV1() {
        return readServiceParams("accounts.service");
    }

    public ServiceParams getAnnotationServiceParamsV1() {
        return readServiceParams("annotation.service");
    }

    public ServiceParams getAnnotationServiceParamsV2() {
        ServiceParams serviceParams = readServiceParams("annotation.service");
        String endpoint = read("annotation.service.v2.url", config::getString, null);
        return new ServiceParams(endpoint, serviceParams.timeout, serviceParams.clientSecret);
    }

    public ServiceParams getConceptServiceParamsV1() {
        return readServiceParams("concept.service");
    }

    public List<String> getConceptServiceTemplateFilters() {
        return read("concept.service.template.filters", config::getStringList, Collections.emptyList());
    }

    public ServiceParams getMediaServiceParamsV1() {
        return readServiceParams("media.service");
    }

    public ServiceParams getPanoptesServiceParamsV1() {
        return readServiceParams("panoptes.service");
    }

    public ServiceParams getPreferencesServiceParamsV1() {
        return readServiceParams("preferences.service");
    }

    public String getAppDefaultsActivity() {
        return read("app.defaults.activity", config::getString, null);
    }

    public String getAppDefaultsCameraId() {
        return read("app.defaults.cameraid", config::getString, null);
    }

    public String getAppDefaultsGroup() {
        return read("app.defaults.group", config::getString, null);
    }

    public String getAppAnnotationIdentityReference() {
        return read("app.annotation.identity.reference", config::getString, "identity-reference");
    }

    public String getAppAnnotationSampleAssociationComment() {
        return read("app.annotation.sample.association.comment", config::getString, "comment");
    }

    public String getAppAnnotationSampleAssociationEquipment() {
        return read("app.annotation.sample.association.equipment", config::getString, "sampled-by");
    }

    public String getAppAnnotationSampleAssociationReference() {
        return read("app.annotation.sample.association.reference", config::getString, "sample-reference");
    }

    public String getAppAnnotationSampleDefaultConcept() {
        return read("app.annotation.sample.default.concept", config::getString, "equipment");
    }

    public String getAppAnnotationUpon() {
        return read("app.annotation.upon", config::getString, "upon");
    }

    public String getAppAnnotationUponRoot() {
        return read("app.annotation.upon.root", config::getString, "physical object");
    }

    public String getAppImageCopyrightOwner() {
        return read("app.image.copyright.owner", config::getString, "");
    }

    public String getAppInjectorModuleClass() {
        return read("app.injector.module.class", config::getString, null);
    }



}
