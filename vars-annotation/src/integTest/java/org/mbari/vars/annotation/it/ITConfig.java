package org.mbari.vars.annotation.it;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.nio.file.Path;

public class ITConfig {

    public static final String CONFIG_PATH = "/reference.conf";

    private final Config config;

    public ITConfig() {
        var url = getClass().getResource(CONFIG_PATH);
        var path = Path.of(url.getPath());
        config = ConfigFactory.parseFile(path.toFile());
    }

    public String pythiaEndpoint() {
        return config.getString("pythia.service.endpoint");
    }
}
