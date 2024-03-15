package org.mbari.vars.ui.commands;

import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.ObservationsUpdate;

import java.util.List;

public class ChangeGroupCmd2 extends UpdateObservationsCmd {

    public ChangeGroupCmd2(List<Annotation> originalAnnotations, String group) {
        super(originalAnnotations, ObservationsUpdate.forGroup(observationUuids(originalAnnotations), group));
    }

    @Override
    public String getDescription() {
        return "Changing group of " + originalAnnotations.size() + " annotations to " + observationsUpdate.group();
    }
}
