package org.mbari.vars.services.model;

public class HealthStatusCheck {
    private String name;
    private HealthStatus healthStatus;

    public HealthStatusCheck() {
    }

    public HealthStatusCheck(String name, HealthStatus healthStatus) {
        this.name = name;
        this.healthStatus = healthStatus;
    }

    public String getName() {
        return name;
    }

    public HealthStatus getHealthStatus() {
        return healthStatus;
    }
}
