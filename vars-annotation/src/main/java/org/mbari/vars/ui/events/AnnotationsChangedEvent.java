package org.mbari.vars.ui.events;

import org.mbari.vars.services.model.Annotation;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Brian Schlining
 * @since 2017-07-20T17:18:00
 */
public class AnnotationsChangedEvent extends UIChangeEvent<Collection<Annotation>> {

    public AnnotationsChangedEvent(Object changeSource, Collection<Annotation> annotations) {
        super(changeSource, Collections.unmodifiableCollection(annotations));
    }

    public AnnotationsChangedEvent(Collection<Annotation> annotations) {
        this(null, annotations);
    }
}
