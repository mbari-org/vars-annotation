package org.mbari.m3.vars.annotation.commands;

import org.mbari.m3.vars.annotation.model.Annotation;

/**
 * Message to notify app that a new annotation was created.
 *
 * @author Brian Schlining
 * @since 2017-05-17T11:08:00
 */
public class NewAnnotation implements NewObjectNotification<Annotation> {

    private final Annotation annotation;

    public NewAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }

    @Override
    public Annotation get() {
        return annotation;
    }
}
