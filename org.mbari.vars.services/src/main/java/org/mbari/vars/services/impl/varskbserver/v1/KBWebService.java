package org.mbari.vars.services.impl.varskbserver.v1;

import org.mbari.vars.services.model.Concept;
import org.mbari.vars.services.model.ConceptAssociationTemplate;
import org.mbari.vars.services.model.ConceptDetails;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.util.List;

/**
 * https://futurestud.io/tutorials/retrofit-add-custom-request-header
 *
 * This is the retrofit interface that defines all the web api calls for the vars-kb-service.
 * Man, does retrofit make this easy.
 *
 * @author Brian Schlining
 * @since 2017-05-11T16:40:00
 */
public interface KBWebService {

    @GET("concept/root")
    Call<ConceptDetails> findRootDetails();

    @GET("phylogeny/down/{name}")
    Call<Concept> findTree(@Path("name") String name);

    @GET("phylogeny/up/{name}")
    Call<Concept> findParentBranch(@Path("name") String name);

    @GET("concept")
    Call<List<String>> findAllNames();

    @GET("concept/{name}")
    Call<ConceptDetails> findDetails(@Path("name") String name);

    @GET("links/")
    Call<List<ConceptAssociationTemplate>> findAllTemplates();

    @GET("links/{name}")
    Call<List<ConceptAssociationTemplate>> findTemplates(@Path("name") String name);

    @GET("links/{name}/using/{linkname}")
    Call<List<ConceptAssociationTemplate>> findTemplates(@Path("name") String name,
                                                         @Path("linkname") String linkname);

}
