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
    private final AppState appState;

    @Inject
    public UIToolBox(AppState appState,
                     EventBus eventBus,
                     ResourceBundle i18nBundle,
                     Config config) {
        this.appState = appState;
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

    public AppState getAppState() {
        return appState;
    }
}
