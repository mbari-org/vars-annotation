package org.mbari.vars.services;

import com.typesafe.ConfigFactory;

public class TestConstants {

    private static Services services;
    public static synchronized Services getServices() {
        if (services == null) {
            var config = ConfigFactory.getCofig();
            services = ServicesBuilder.build(config);
        }
        return services;
    }
}
