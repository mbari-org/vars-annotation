package org.mbari.vars.annotation;

import com.typesafe.config.Config;
import org.mbari.vars.annotation.etc.jdk.Loggers;

import java.util.Collections;
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


    public AppConfig(Config config) {
        this.config = config;
    }

    public int getSharktopodaDefaultsControlPort() {
        return read("sharktopoda.defaults.control.port", config::getInt, 8800);
    }

    public int getSharktopodaDefaultsFramegrabPort() {
        return read("sharktopoda.defaults.framegrab.port", config::getInt, 5001);
    }

    public int getLocalizationDefaultsIncomingPort() {
        return read("localization.defaults.incoming.port", config::getInt, 5562);
    }

    public String getLocalizationDefaultsIncomingTopic() {
        return read("localization.defaults.incoming.topic", config::getString, "localization");
    }

    public int getLocalizationDefaultsOutgoingPort() {
        return read("localization.defaults.outgoing.port", config::getInt, 5561);
    }

    public String getLocalizationDefaultsOutgoingTopic() {
        return read("localization.defaults.outgoing.topic", config::getString, "localization");
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
