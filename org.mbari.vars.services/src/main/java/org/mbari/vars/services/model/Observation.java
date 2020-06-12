package org.mbari.vars.services.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class Observation {
    private String concept;
    private Instant observationTimestamp;
    private String observer;
    private String group;
    private String activity;
    private List<Association> associations;
    private Instant lastUpdatedTime;
    private UUID uuid;

    public Observation() {

    }

    public Observation(String concept, Instant observationTimestamp, String observer, String group,
                       String activity, List<Association> associations, Instant lastUpdatedTime,
                       UUID uuid) {
        this.concept = concept;
        this.observationTimestamp = observationTimestamp;
        this.observer = observer;
        this.group = group;
        this.activity = activity;
        this.associations = associations;
        this.lastUpdatedTime = lastUpdatedTime;
        this.uuid = uuid;
    }

    public String getConcept() {
        return concept;
    }

    public Instant getObservationTimestamp() {
        return observationTimestamp;
    }

    public String getObserver() {
        return observer;
    }

    public String getGroup() {
        return group;
    }

    public String getActivity() {
        return activity;
    }

    public List<Association> getAssociations() {
        return associations;
    }

    public Instant getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public UUID getUuid() {
        return uuid;
    }
}
