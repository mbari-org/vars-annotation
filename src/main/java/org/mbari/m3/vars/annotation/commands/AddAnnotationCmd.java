package org.mbari.m3.vars.annotation.commands;

import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Annotation;

import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2017-07-20T17:37:00
 */
public class AddAnnotationCmd implements Command {
    private final Annotation annotationTemplate;
    private UUID annotationUuid;

    public AddAnnotationCmd(Annotation annotationTemplate) {
        this.annotationTemplate = annotationTemplate;
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
