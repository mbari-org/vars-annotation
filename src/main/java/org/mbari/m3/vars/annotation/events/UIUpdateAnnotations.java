package org.mbari.m3.vars.annotation.events;

import com.google.common.base.Preconditions;
import org.mbari.m3.vars.annotation.commands.NewObjectNotification;
import org.mbari.m3.vars.annotation.model.Annotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class to tell the UI to redraw  an annotation that has been updated.
 * @author Brian Schlining
 * @since 2017-07-20T15:55:00
 */
public class UIUpdateAnnotations implements NewObjectNotification<List<Annotation>> {

    private final List<Annotation> annotations;

    public UIUpdateAnnotations(List<Annotation> annotations) {
        Preconditions.checkArgument(annotations != null, "Annotations can not be null");
        this.annotations = Collections.unmodifiableList(new ArrayList<>(annotations));
    }

    @Override
    public List<Annotation> get() {
        return null;
    }
}
