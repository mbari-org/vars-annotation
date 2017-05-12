package org.mbari.m3.vars.annotation.services.varskbserver;

import org.mbari.m3.vars.annotation.model.Concept;
import org.mbari.m3.vars.annotation.model.ConceptDetails;
import org.mbari.m3.vars.annotation.services.ConceptService;
import retrofit2.Retrofit;

import java.util.List;
import java.util.Optional;

/**
 * @author Brian Schlining
 * @since 2017-05-11T16:13:00
 */
public class KBConceptService implements ConceptService {

    @Override
    public Concept fetchConceptTree() {
        return null;
    }

    @Override
    public Optional<ConceptDetails> findDetails(String name) {
        return null;
    }

    @Override
    public List<String> fetchAllNames() {
        return null;
    }


}
