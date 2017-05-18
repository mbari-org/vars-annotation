package org.mbari.m3.vars.annotation.commands;

import org.mbari.m3.vars.annotation.model.Association;

/**
 * Message to notify app that a new association was created.
 *
 * @author Brian Schlining
 * @since 2017-05-17T11:08:00
 */
public class NewAssociation implements NewObjectNotification<Association> {

    private final Association association;

    public NewAssociation(Association association) {
        this.association = association;
    }

    @Override
    public Association get() {
        return association;
    }
}
