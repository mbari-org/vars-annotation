package org.mbari.vars.services.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EndpointStatus {
    private EndpointConfig endpointConfig;
    private HealthStatusCheck healthStatusCheck;

    public EndpointStatus() {
    }

    public EndpointStatus(EndpointConfig endpointConfig, HealthStatusCheck healthStatusCheck) {
        this.endpointConfig = endpointConfig;
        this.healthStatusCheck = healthStatusCheck;
    }

    public EndpointConfig getEndpointConfig() {
        return endpointConfig;
    }

    public HealthStatusCheck getHealthStatusCheck() {
        return healthStatusCheck;
    }

    public static Set<EndpointStatus> collate(List<EndpointConfig> configs, List<HealthStatusCheck> checks) {

        var results = new HashSet<EndpointStatus>();
        for (var config : configs) {
            var match = checks.stream()
                    .filter(c -> c.getName().equalsIgnoreCase(config.getName()))
                    .findFirst();
            if (match.isPresent()) {
                results.add(new EndpointStatus(config, match.get()));
            }
            else {
                results.add(new EndpointStatus(config, null));
            }
        }
        return results;
    }

    public boolean isHealthy() {
        return healthStatusCheck != null && healthStatusCheck.getHealthStatus() != null;
    }


}
