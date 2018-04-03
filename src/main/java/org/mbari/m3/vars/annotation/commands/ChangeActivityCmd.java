package org.mbari.m3.vars.annotation.commands;

import org.mbari.m3.vars.annotation.model.Annotation;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-09-15T19:32:00
 */
public class ChangeActivityCmd extends UpdateAnnotationsCmd {

    private final String activity;


    public ChangeActivityCmd(List<Annotation> originalAnnotations, String activity) {
        super(originalAnnotations, originalAnnotations.stream()
                .map(Annotation::new)
                .peek(a -> a.setActivity(activity))
                .collect(Collectors.toList()), false, false);
        this.activity = activity;
    }


    @Override
    public String getDescription() {
        return "Changing activity of " + originalAnnotations.size() + " annotations to " + activity;
    }
}