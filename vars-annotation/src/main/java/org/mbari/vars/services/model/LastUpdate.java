package org.mbari.vars.services.model;

import java.time.Instant;

/**
 * @author Brian Schlining
 * @since 2019-01-31T14:36:00
 */
public class LastUpdate {
    private Instant timestamp;

    public LastUpdate() {
    }

    public LastUpdate(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
