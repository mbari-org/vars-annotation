package org.mbari.m3.vars.annotation.services.varskbserver.v1;

import org.mbari.m3.vars.annotation.model.Concept;
import org.mbari.m3.vars.annotation.model.ConceptAssociationTemplate;
import org.mbari.m3.vars.annotation.model.ConceptDetails;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.util.List;

/**
 * https://futurestud.io/tutorials/retrofit-add-custom-request-header
 *
 * @author Brian Schlining
 * @since 2017-05-11T16:40:00
 */
public interface KBService {

    @GET("phylogeny/down/object")
    Call<Concept> findRoot();

    @GET("concept")
    Call<List<String>> listConceptNames();

    @GET("concept/{name}")
    Call<ConceptDetails> findDetails(@Path("name") String name);

    @GET("links/{name}")
    Call<List<ConceptAssociationTemplate>> findTemplates(@Path("name") String name);

}
