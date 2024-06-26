package org.mbari.vars.ui.commands;

import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.ObservationsUpdate;

import java.util.List;

public class ChangeActivityCmd extends UpdateObservationsCmd {



    public ChangeActivityCmd(List<Annotation> originalAnnotations, String activity) {
        super(originalAnnotations, ObservationsUpdate.forActivity(observationUuids(originalAnnotations), activity));
    }

    @Override
    public String getDescription() {
        return "Changing activity of " + originalAnnotations.size() + " annotations to " + observationsUpdate.activity();
    }
}
