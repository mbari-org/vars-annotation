package org.mbari.vars.annotation.services.oni;

import com.typesafe.config.Config;
import org.mbari.vars.oni.sdk.r1.ConceptService;
import org.mbari.vars.oni.sdk.r1.models.Concept;
import org.mbari.vars.oni.sdk.r1.models.ConceptAssociationTemplate;
import org.mbari.vars.oni.sdk.r1.models.ConceptDetails;
import org.mbari.vars.annotation.etc.jdk.Loggers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The base ConceptService should read data AS IS from the Knowledgebase. This
 * version decorates a conceptService and may tweak the data somewhat as it's
 * read, for example filtering out unwanted link-templates
 *
 * @author Brian Schlining
 * @since 2018-10-23T11:53:00
 */
public class ModifyingConceptService implements ConceptService {

    private final ConceptService conceptService;
    private final Loggers log = new Loggers(getClass());

    /** Not all link-templates may need to be displayed in the UI */
    private List<Pattern> templateFilters;
    private static final String CONFIG_KEY = "concept.service.template.filters";

    /**
     *
     * @param conceptService
     * @param config
     * @deprecated
     */
    public ModifyingConceptService(ConceptService conceptService, Config config) {
        this.conceptService = conceptService;
        try {
            List<String> regex = config.getStringList(CONFIG_KEY);
            templateFilters = regex.stream()
                    .map(Pattern::compile)
                    .collect(Collectors.toList());
        }
        catch (Exception e){
            log.atInfo().withCause(e).log("Error reading configuration defined for " + CONFIG_KEY);
            templateFilters = new ArrayList<>();
        }
    }

    public ModifyingConceptService(ConceptService conceptService, List<String> associationFilterRegex) {
        this.conceptService = conceptService;
        try {
            List<String> regex = associationFilterRegex;
            templateFilters = regex.stream()
                    .map(Pattern::compile)
                    .collect(Collectors.toList());
        }
        catch (Exception e){
            log.atInfo().withCause(e).log("Error reading configuration defined for " + CONFIG_KEY);
            templateFilters = new ArrayList<>();
        }
    }


    @Override
    public CompletableFuture<Concept> findRoot() {
        return conceptService.findRoot();
    }

    @Override
    public CompletableFuture<Optional<ConceptDetails>> findDetails(String name) {
        return conceptService.findDetails(name);
    }

    @Override
    public CompletableFuture<ConceptDetails> findRootDetails() {
        return conceptService.findRootDetails();
    }

    @Override
    public CompletableFuture<List<String>> findAllNames() {
        return conceptService.findAllNames();
    }

    @Override
    public CompletableFuture<List<ConceptAssociationTemplate>> findAllTemplates() {
        return conceptService.findAllTemplates()
                .thenApply(this::filter);
    }

    @Override
    public CompletableFuture<List<ConceptAssociationTemplate>> findTemplates(String name) {
        return conceptService.findTemplates(name)
                .thenApply(this::filter);
    }

    @Override
    public CompletableFuture<List<ConceptAssociationTemplate>> findTemplates(String name, String linkname) {
        return conceptService.findTemplates(name, linkname)
                .thenApply(this::filter);
    }

    @Override
    public CompletableFuture<Optional<Concept>> findConcept(String name) {
        return conceptService.findConcept(name);
    }

    @Override
    public CompletableFuture<Optional<Concept>> findParent(String name) {
        return conceptService.findParent(name);
    }

    private List<ConceptAssociationTemplate> filter(List<ConceptAssociationTemplate> templates) {
        return templates.stream()
                .filter(t -> {
                    String name = t.getLinkName();
                    boolean keep = true;
                    for (Pattern p : templateFilters) {
                        Matcher matcher = p.matcher(name);
                        if (matcher.matches()) {
                            keep = false;
                            break;
                        }
                    }
                    return keep;
                })
                .collect(Collectors.toList());
    }
}
