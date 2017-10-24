package org.mbari.m3.vars.annotation.commands;


import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Annotation;

import java.time.Duration;
import java.util.List;

/**
 * @author Brian Schlining
 * @since 2017-10-23T15:31:00
 */
public class SetDurationUsingElapsedTimeCmd implements Command {
    private final List<Annotation> annotations;
    private final Duration elapsedTime;

    public SetDurationUsingElapsedTimeCmd(List<Annotation> annotations, Duration elapsedTime) {
        this.annotations = annotations;
        this.elapsedTime = elapsedTime;
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
