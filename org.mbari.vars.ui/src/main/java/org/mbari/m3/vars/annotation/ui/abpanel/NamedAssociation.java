package org.mbari.m3.vars.annotation.ui.abpanel;

import org.mbari.vars.services.model.Association;
import org.mbari.vars.services.model.ConceptAssociationTemplate;

/**
 * @author Brian Schlining
 * @since 2017-10-12T15:49:00
 */
class NamedAssociation extends Association {
    private final String name;

    public NamedAssociation(String linkName, String toConcept, String linkValue, String name) {
        super(linkName, toConcept, linkValue);
        this.name = name;
    }

    public NamedAssociation(ConceptAssociationTemplate cat, String name) {
        super(cat.getLinkName(), cat.getToConcept(), cat.getLinkValue());
        this.name = name;
    }

    public String getName() {
        return name;
    }
}