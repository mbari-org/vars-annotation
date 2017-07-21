package org.mbari.m3.vars.annotation;


import com.typesafe.config.Config;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.mbari.m3.vars.annotation.services.VideoControlService;

import javax.inject.Inject;
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
    private final ObjectProperty<VideoControlService> videoControlService = new SimpleObjectProperty<>();

    @Inject
    public UIToolBox(Data data,
                     Services services,
                     EventBus eventBus,
                     ResourceBundle i18nBundle,
                     Config config) {
        this.data = data;
        this.services = services;
        this.eventBus = eventBus;
        this.i18nBundle = i18nBundle;
        this.config = config;
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

    public VideoControlService getVideoControlService() {
        return videoControlService.get();
    }

    public ObjectProperty<VideoControlService> videoControlServiceProperty() {
        return videoControlService;
    }
}
