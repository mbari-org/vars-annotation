package org.mbari.vars.ui.commands;

import org.mbari.vars.services.model.Annotation;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-09-18T10:46:00
 * @deprecated use {@link ChangeConceptCmd2} instead
 */
public class ChangeConceptCmd extends UpdateAnnotationsCmd {

    private final String concept;

    public ChangeConceptCmd(List<Annotation> originalAnnotations, String concept) {
        super(originalAnnotations, originalAnnotations.stream()
                .map(Annotation::new)
                .peek(a -> a.setConcept(concept))
                .collect(Collectors.toList()), true, true);
        this.concept = concept;
    }

    @Override
    public String getDescription() {
        return "Changing concept for " + originalAnnotations.size() + " annotations to " + concept;
    }

}
