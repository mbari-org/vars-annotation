package org.mbari.m3.vars.annotation.commands;

import org.mbari.m3.vars.annotation.model.Association;

import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2017-05-10T10:02:00
 */
public class CreateAssociation implements Command {

    private final UUID observationUuid;
    private final Association association;

    public CreateAssociation(UUID observationUuid, Association association) {
        this.observationUuid = observationUuid;
        this.association = association;
    }

    @Override
    public void apply() {

    }

    @Override
    public void unapply() {

    }

    @Override
    public String getDescription() {
        return null;
    }
}
