package org.mbari.m3.vars.annotation.commands;

import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Annotation;

/**
 * @author Brian Schlining
 * @since 2017-05-10T10:05:00
 */
public class UpdateAnnotationCmd implements Command {

    private final Annotation oldAnnotation;
    private final Annotation newAnnotation;

    public UpdateAnnotationCmd(Annotation oldAnnotation, Annotation newAnnotation) {
        this.oldAnnotation = oldAnnotation;
        this.newAnnotation = newAnnotation;
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

    @Override
    public String toString() {
        return "UpdateAnnotationCmd{" +
                "oldAnnotation=" + oldAnnotation +
                ", newAnnotation=" + newAnnotation +
                '}';
    }
}