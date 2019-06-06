package org.mbari.m3.vars.annotation.model;

import java.util.List;

/**
 * @author Brian Schlining
 * @since 2019-06-05T16:44:00
 */
public class ConceptAssociationResponse {
    private ConceptAssociationRequest conceptAssociationRequest;
    private List<ConceptAssociation> conceptAssociations;

    public ConceptAssociationResponse(ConceptAssociationRequest conceptAssociationRequest,
                                      List<ConceptAssociation> conceptAssociations) {
        this.conceptAssociationRequest = conceptAssociationRequest;
        this.conceptAssociations = conceptAssociations;
    }

    public ConceptAssociationRequest getConceptAssociationRequest() {
        return conceptAssociationRequest;
    }

    public List<ConceptAssociation> getConceptAssociations() {
        return conceptAssociations;
    }
}
