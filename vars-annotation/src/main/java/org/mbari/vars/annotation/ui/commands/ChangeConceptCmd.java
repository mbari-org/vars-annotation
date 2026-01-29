package org.mbari.vars.annotation.ui.commands;

import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.oni.sdk.r1.models.User;
import org.mbari.vars.services.model.ObservationsUpdate;
import org.mbari.vars.annotation.ui.UIToolBox;

import java.util.List;

public class ChangeConceptCmd extends UpdateObservationsCmd {

    public ChangeConceptCmd(List<Annotation> originalAnnotations, String concept) {
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
