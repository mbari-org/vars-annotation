package org.mbari.m3.vars.annotation.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2017-06-28T13:09:00
 */
public class ChangeVideoReferenceUuids {


    private final List<UUID> uuids;

    public ChangeVideoReferenceUuids(List<UUID> uuids) {
        this.uuids = Collections.unmodifiableList(new ArrayList<>(uuids));
    }

    public List<UUID> getUuids() {
        return uuids;
    }
}
