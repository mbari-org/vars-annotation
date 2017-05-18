package org.mbari.m3.vars.annotation.commands;

import org.mbari.m3.vars.annotation.model.Annotation;

/**
 * @author Brian Schlining
 * @since 2017-05-10T10:02:00
 */
public class CreateAnnotation implements Command {

    private final String name;
    private Annotation annotation;

    public CreateAnnotation(String name) {
        this.name = name;
    }

    @Override
    public void apply() {
        // TODO Grab time from video
        // TODO insert into database
        // TODO notify app of new annotation.
        // TODO after creating it save it to annotaiton for undo.
    }

    @Override
    public void unapply() {

    }

    @Override
    public String getDescription() {
        return null;
    }
}