package org.mbari.m3.vars.annotation.commands;

import com.google.common.collect.ImmutableList;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Association;

import java.util.Collection;

/**
 * @author Brian Schlining
 * @since 2017-07-20T17:35:00
 */
public class AddAssociationCmd {

    private final Association associationTemplate;
    private final Collection<Annotation> originalAnnotations;

    public AddAssociationCmd(Association associationTemplate, Collection<Annotation> originalAnnotations) {
        this.associationTemplate = associationTemplate;
        this.originalAnnotations = ImmutableList.copyOf(originalAnnotations);
    }
}
