package org.mbari.m3.vars.annotation.commands;

import org.mbari.m3.vars.annotation.model.Association;

/**
 * @author Brian Schlining
 * @since 2017-05-17T11:09:00
 */
public class NewDescription implements NewObjectNotification<Association> {

    private final Association association;

    public NewDescription(Association association) {
        this.association = association;
    }

    @Override
    public Association get() {
        return association;
    }
}
