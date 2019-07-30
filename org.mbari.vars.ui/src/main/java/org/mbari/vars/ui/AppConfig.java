package org.mbari.vars.ui;

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



    public AppConfig(Config config) {
        super(config);
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
