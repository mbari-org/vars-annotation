package org.mbari.vars.annotation.ui.events;

import java.util.UUID;

/**
 * The video player has completed its asynchronous processing of an open command,
 * reported via an 'open done' message.
 */
public class OpenDoneEvent {

    private final UUID uuid;

    public OpenDoneEvent(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }
}
