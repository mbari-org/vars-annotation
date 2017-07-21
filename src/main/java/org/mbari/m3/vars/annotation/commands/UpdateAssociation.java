package org.mbari.m3.vars.annotation.commands;

import org.mbari.m3.vars.annotation.model.Association;

/**
 * @author Brian Schlining
 * @since 2017-05-10T10:06:00
 */
public class UpdateAssociation implements Command {

    private final Association association;

    public UpdateAssociation(Association association) {
        this.association = association;
    }

    @Override
    public void apply() {
        // Do upda
    }

    @Override
    public void unapply() {

    }

    @Override
    public String getDescription() {
        return null;
    }
}