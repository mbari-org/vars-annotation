package org.mbari.vars.services.model;

import java.net.URL;
import java.time.Duration;

public class EndpointConfig {
    private String name;
    private URL url;
    private Duration timeout;
    private String proxyPath;
    private String secret;

    public EndpointConfig() {
    }

    public EndpointConfig(String name, URL url, Duration timeout, String proxyPath, String secret) {
        this.name = name;
        this.url = url;
        this.timeout = timeout;
        this.proxyPath = proxyPath;
        this.secret = secret;
    }

    public String getName() {
        return name;
    }

    public URL getUrl() {
        return url;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public String getProxyPath() {
        return proxyPath;
    }

    public String getSecret() {
        return secret;
    }
}
