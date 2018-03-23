package org.mbari.m3.vars.annotation.events;

import org.mbari.m3.vars.annotation.model.Annotation;

import java.util.*;

/**
 * @author Brian Schlining
 * @since 2017-06-28T13:02:00
 */
public class AnnotationsSelectedEvent extends UISelectionEvent<Collection<Annotation>> {

    public AnnotationsSelectedEvent(Object selectionSource, Collection<Annotation> annotations) {
        super(selectionSource, Collections.unmodifiableCollection(annotations));
    }

    public AnnotationsSelectedEvent(Collection<Annotation> annotations) {
        this(null, annotations);
    }

    public AnnotationsSelectedEvent(Annotation annotation) {
        this(null, Collections.singletonList(annotation));
    }
}
