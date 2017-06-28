package org.mbari.m3.vars.annotation.commands;

import org.mbari.m3.vars.annotation.model.Annotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Brian Schlining
 * @since 2017-06-28T13:02:00
 */
public class SelectedAnnotations {
    private final List<Annotation> annotations;

    public SelectedAnnotations(List<Annotation> annotations) {
        this.annotations = Collections.unmodifiableList(new ArrayList<>(annotations));
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }
}
