package org.mbari.m3.vars.annotation;


import com.typesafe.config.Config;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.mbari.m3.vars.annotation.services.VideoControlService;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoState;

import javax.inject.Inject;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.ResourceBundle;

/**
 * @author Brian Schlining
 * @since 2017-06-10T12:06:00
 */
public class UIToolBox {

    private final EventBus eventBus;
    private final ResourceBundle i18nBundle;
    private final Config config;
    private final Data data;
    private final Services services;
    private final ObjectProperty<VideoControlService<? extends VideoState, ? extends VideoError>> videoControlService = new SimpleObjectProperty<>();

    /** URL to the stylesheet used for the apps */
    private final Collection<String> stylesheets;

    @Inject
    public UIToolBox(Data data,
                     Services services,
                     EventBus eventBus,
                     ResourceBundle i18nBundle,
                     Config config,
                     Collection<String> stylesheets) {
        this.data = data;
        this.services = services;
        this.eventBus = eventBus;
        this.i18nBundle = i18nBundle;
        this.config = config;
        this.stylesheets = Collections.unmodifiableCollection(stylesheets);
    }

    public Collection<String> getStylesheets() {
        return stylesheets;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public ResourceBundle getI18nBundle() {
        return i18nBundle;
    }

    public Config getConfig() {
        return config;
    }

    public Data getData() {
        return data;
    }

    public Services getServices() {
        return services;
    }

    public VideoControlService<? extends VideoState, ? extends VideoError> getVideoControlService() {
        return videoControlService.get();
    }

    public ObjectProperty<VideoControlService<? extends VideoState, ? extends VideoError>> videoControlServiceProperty() {
        return videoControlService;
    }
}
