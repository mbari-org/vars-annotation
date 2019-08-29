package org.mbari.vars.services;


import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.mbari.vars.core.EventBus;

/**
 * @author Brian Schlining
 * @since 2019-08-28T16:44:00
 */
public class TestToolbox {

    private static final EventBus eventBus = new EventBus();
    private static final Config config = ConfigFactory.load();
    private static final Services services = ServicesBuilder.build(config);

    public static EventBus getEventBus() {
        return eventBus;
    }

    public static Config getConfig() {
        return config;
    }

    public static Services getServices() {
        return services;
    }
}
