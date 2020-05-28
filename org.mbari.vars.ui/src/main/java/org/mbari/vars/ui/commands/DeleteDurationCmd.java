package org.mbari.vars.ui.commands;

import org.mbari.vars.services.model.Annotation;
import java.util.List;
import java.util.stream.Collectors;

public class DeleteDurationCmd extends UpdateAnnotationsCmd {

    public DeleteDurationCmd(List<Annotation> annotations) {
        super(annotations.stream()
                .filter(a -> a.getDuration() != null)
                .collect(Collectors.toList()),
              annotations.stream()
                .filter(a -> a.getDuration() != null)
                .map(Annotation::new)
                .peek(a -> a.setDuration(null))
                .collect(Collectors.toList()),
            false,
            false);
    }

    @Override
    public String getDescription() {
        return "Delete duration from " + originalAnnotations.size() + " annotations";
    }

}
