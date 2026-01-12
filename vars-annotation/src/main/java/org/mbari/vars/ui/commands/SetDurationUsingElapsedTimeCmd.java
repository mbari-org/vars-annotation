package org.mbari.vars.ui.commands;


import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.services.model.Annotation;

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
