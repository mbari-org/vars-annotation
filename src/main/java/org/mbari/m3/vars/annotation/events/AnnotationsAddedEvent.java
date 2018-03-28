package org.mbari.m3.vars.annotation.events;

import com.google.common.collect.ImmutableList;
import org.mbari.m3.vars.annotation.model.Annotation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Brian Schlining
 * @since 2017-07-20T17:12:00
 */
public class AnnotationsAddedEvent extends UIEvent<Collection<Annotation>> {

    public AnnotationsAddedEvent(Object source, Collection<Annotation> annotations) {
        super(source, ImmutableList.copyOf(annotations));
    }

    public AnnotationsAddedEvent(Collection<Annotation> annotations) {
        this(null, annotations);
    }

    public AnnotationsAddedEvent(Annotation annotation) {
        this(null, Collections.singletonList(annotation));
    }
}
