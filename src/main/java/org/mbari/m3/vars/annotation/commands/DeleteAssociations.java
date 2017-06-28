package org.mbari.m3.vars.annotation.commands;

import org.mbari.m3.vars.annotation.model.Association;

import java.util.List;

/**
 * @author Brian Schlining
 * @since 2017-05-11T13:06:00
 */
public class DeleteAssociations implements Command {

    private final List<Association> associations;

    public DeleteAssociations(List<Association> associations) {
        this.associations = associations;
    }

    @Override
    public void apply() {

    }

    @Override
    public void unapply() {

    }

    @Override
    public String getDescription() {
        return "Delete Associations";
    }

    // TODO annosaurus should support deletion of multiple associations in one transaction
}
