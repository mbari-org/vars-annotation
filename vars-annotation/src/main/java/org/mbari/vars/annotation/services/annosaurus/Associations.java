package org.mbari.vars.annotation.services.annosaurus;

import org.mbari.vars.annosaurus.sdk.r1.models.Association;

public class Associations {

    public static Association fromDetails(org.mbari.vars.annosaurus.sdk.r1.models.Details d) {
        return new Association(d.getLinkName(), d.getToConcept(), d.getLinkValue());
    }

    public static Association fromDetails(org.mbari.vars.oni.sdk.r1.models.Details d) {
        return new Association(d.getLinkName(), d.getToConcept(), d.getLinkValue());
    }
}
