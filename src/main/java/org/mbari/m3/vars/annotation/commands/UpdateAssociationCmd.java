package org.mbari.m3.vars.annotation.commands;

import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Association;

/**
 * @author Brian Schlining
 * @since 2017-05-10T10:06:00
 */
public class UpdateAssociationCmd implements Command {

    private final Association association;

    public UpdateAssociationCmd(Association association) {
        this.association = association;
    }

    @Override
    public void apply(UIToolBox toolBox) {

    }

    @Override
    public void unapply(UIToolBox toolBox) {

    }

    @Override
    public String getDescription() {
        return null;
    }
}