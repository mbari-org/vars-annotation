package org.mbari.vars.ui.commands;

import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.ObservationsUpdate;
import org.mbari.vars.services.model.User;
import org.mbari.vars.ui.UIToolBox;

import java.util.List;

public class ChangeConceptCmd2 extends UpdateObservationsCmd {

    public ChangeConceptCmd2(List<Annotation> originalAnnotations, String concept) {
        super(originalAnnotations, ObservationsUpdate.forConcept(observationUuids(originalAnnotations), concept));
    }

    @Override
    public void apply(UIToolBox toolBox) {
        final User user = toolBox.getData().getUser();
        if (user != null) {
            observationsUpdate = observationsUpdate.withObserver(user.getUsername());
        }
        super.apply(toolBox);
    }

    @Override
    public String getDescription() {
        return "Changing concept of " + originalAnnotations.size() + " annotations to " + observationsUpdate.concept();
    }
}
