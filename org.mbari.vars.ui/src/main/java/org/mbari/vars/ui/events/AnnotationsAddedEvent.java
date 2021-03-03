package org.mbari.vars.ui.events;

import org.mbari.vars.services.model.Annotation;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Brian Schlining
 * @since 2017-07-20T17:12:00
 */
public class AnnotationsAddedEvent extends UIEvent<Collection<Annotation>> {

    public AnnotationsAddedEvent(Object source, Collection<Annotation> annotations) {
        super(source, Collections.unmodifiableCollection(annotations));
    }

    public AnnotationsAddedEvent(Collection<Annotation> annotations) {
        this(null, annotations);
    }

    public AnnotationsAddedEvent(Annotation annotation) {
        this(null, Collections.singletonList(annotation));
    }
}
