package org.mbari.vars.services.model;

import java.util.Collection;
import java.util.UUID;

public record ObservationsUpdate(Collection<UUID> observationUuids, String concept, String observer, String group, String activity) {


    public static ObservationsUpdate forActivity(Collection<UUID> observationUuids, String activity) {
        return new ObservationsUpdate(observationUuids, null, null, null, activity);
    }

    public static ObservationsUpdate forConcept(Collection<UUID> observationUuids, String concept) {
        return new ObservationsUpdate(observationUuids, concept, null, null, null);
    }

    public static ObservationsUpdate forGroup(Collection<UUID> observationUuids, String group) {
        return new ObservationsUpdate(observationUuids, null, null, group, null);
    }

    public static ObservationsUpdate forObserver(Collection<UUID> observationUuids, String observer) {
        return new ObservationsUpdate(observationUuids, null, observer, null, null);
    }

    public static ObservationsUpdate forConceptAndObserver(Collection<UUID> observationUuids, String concept, String observer) {
        return new ObservationsUpdate(observationUuids, concept, observer, null, null);
    }

    public ObservationsUpdate withConcept(String concept) {
        return new ObservationsUpdate(observationUuids, concept, observer, group, activity);
    }

    public ObservationsUpdate withObserver(String observer) {
        return new ObservationsUpdate(observationUuids, concept, observer, group, activity);
    }

}
