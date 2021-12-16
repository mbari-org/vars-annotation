package org.mbari.vars.services.model;

import com.google.gson.annotations.SerializedName;

import java.net.URL;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class EndpointConfig {
    private String name;
    private URL url;

    @SerializedName("timeoutMillis")
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

    public EndpointConfig(EndpointConfig src, String newSecret) {
        this.name = src.name;
        this.url = src.url;
        this.timeout = src.timeout;
        this.proxyPath = src.proxyPath;
        this.secret = newSecret;
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

    /**
     * For an endpoint to be displayed as OK, it needs to have an access secret. This method adds
     * an empty secret to endpoints that do no use access secrets.
     * @param configs
     * @return
     */
    public static List<EndpointConfig> decoratedNoWriteEndpoints(Collection<EndpointConfig> configs) {
        var noSecretNames = List.of("charybdis", "vars-kb-server");
        return configs.stream()
                .map(ec -> {
                    if (noSecretNames.contains(ec.name)) {
                        return new EndpointConfig(ec, "");
                    }
                    return ec;
                })
                .collect(Collectors.toList());
    }
}
