package org.mbari.vars.ui.events;

import org.mbari.vars.services.model.Annotation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Brian Schlining
 * @since 2017-07-20T17:20:00
 */
public class AnnotationsRemovedEvent extends UIEvent<Collection<Annotation>> {

    public AnnotationsRemovedEvent(Object source, Collection<Annotation> annotations) {
        super(source, Collections.unmodifiableCollection(annotations));
    }

    public AnnotationsRemovedEvent(Collection<Annotation> annotations) {
        this(null, annotations);
    }

    public AnnotationsRemovedEvent(Annotation annotation) {
        this(null, Arrays.asList(annotation));
    }
}
