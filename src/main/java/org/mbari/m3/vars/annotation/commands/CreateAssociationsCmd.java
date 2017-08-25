package org.mbari.m3.vars.annotation.commands;

import com.google.common.collect.ImmutableList;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Association;

import java.util.Collection;

/**
 * @author Brian Schlining
 * @since 2017-07-20T17:35:00
 */
public class CreateAssociationsCmd implements Command {

    private final Association associationTemplate;
    private final Collection<Annotation> originalAnnotations;

    public CreateAssociationsCmd(Association associationTemplate, Collection<Annotation> originalAnnotations) {
        this.associationTemplate = associationTemplate;
        this.originalAnnotations = ImmutableList.copyOf(originalAnnotations);
    }

    public Association getAssociationTemplate() {
        return associationTemplate;
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
