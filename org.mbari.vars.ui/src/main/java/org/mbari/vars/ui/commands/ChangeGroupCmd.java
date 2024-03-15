package org.mbari.vars.ui.commands;

import org.mbari.vars.services.model.Annotation;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-09-15T19:31:00
 * @deprecated use {@link ChangeGroupCmd2} instead
 */
public class ChangeGroupCmd extends UpdateAnnotationsCmd {

    private final String group;


    public ChangeGroupCmd(List<Annotation> originalAnnotations, String group) {
        super(originalAnnotations, originalAnnotations.stream()
            .map(a -> new Annotation(a))
            .peek(a -> a.setGroup(group))
            .collect(Collectors.toList()), false, false);
        this.group = group;
    }


    @Override
    public String getDescription() {
        return "Changing group of " + originalAnnotations.size() + " annotations to " + group;
    }
}
