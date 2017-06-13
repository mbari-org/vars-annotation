package org.mbari.m3.vars.annotation;


import com.typesafe.config.Config;
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
}
