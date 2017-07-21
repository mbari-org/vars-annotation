package org.mbari.m3.vars.annotation.commands;

import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Annotation;

import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2017-05-10T10:02:00
 */
public class CreateAnnotationCmd implements Command {
    private final Annotation annotationTemplate;
    private UUID annotationUuid;

    public CreateAnnotationCmd(Annotation annotationTemplate) {
        this.annotationTemplate = annotationTemplate;
    }

    @Override
    public void apply(UIToolBox toolBox) {
        // Timecode/elapsedtime should have already been captured  from video
        // TODO insert into database
        // TODO notify app of new annotation.
        // TODO after creating it save it to annotaiton for undo.
    }

    @Override
    public void unapply(UIToolBox toolBox) {

    }


    @Override
    public String getDescription() {
        return null;
    }
}