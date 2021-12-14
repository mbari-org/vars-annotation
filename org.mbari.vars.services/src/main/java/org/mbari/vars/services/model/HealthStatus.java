package org.mbari.vars.services.model;

public class HealthStatus {
    private String jdkVersion;
    private Integer availableProcessors;
    private Long freeMemory;
    private Long maxMemory;
    private Long totalMemory;
    private String application;
    private String version;

    public HealthStatus() {
    }

    public HealthStatus(String jdkVersion, Integer availableProcessors, Long freeMemory, Long maxMemory, Long totalMemory, String application, String version) {
        this.jdkVersion = jdkVersion;
        this.availableProcessors = availableProcessors;
        this.freeMemory = freeMemory;
        this.maxMemory = maxMemory;
        this.totalMemory = totalMemory;
        this.application = application;
        this.version = version;
    }

    public String getJdkVersion() {
        return jdkVersion;
    }

    public Integer getAvailableProcessors() {
        return availableProcessors;
    }

    public Long getFreeMemory() {
        return freeMemory;
    }

    public Long getMaxMemory() {
        return maxMemory;
    }

    public Long getTotalMemory() {
        return totalMemory;
    }

    public String getApplication() {
        return application;
    }

    public String getVersion() {
        return version;
    }
}
