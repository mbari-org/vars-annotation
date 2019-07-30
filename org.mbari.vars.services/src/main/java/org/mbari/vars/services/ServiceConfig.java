package org.mbari.vars.services;

import com.typesafe.config.Config;
import org.mbari.vars.core.BaseConfig;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

public class ServiceConfig extends BaseConfig {

    public enum PagingStyle {
        PARALLEL,
        SEQUENTIAL
    }

    public static class ServiceParams {
        private final String endpoint;
        private final Duration timeout;
        private final String clientSecret;

        public ServiceParams(String endpoint, Duration timeout, String clientSecret) {
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

    public ServiceConfig(Config config) {
        super(config);
    }

    public ServiceParams readServiceParams(String basePath) {
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
}
