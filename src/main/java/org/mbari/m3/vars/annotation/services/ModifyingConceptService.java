package org.mbari.m3.vars.annotation.services;

import com.typesafe.config.Config;
import org.mbari.m3.vars.annotation.model.Concept;
import org.mbari.m3.vars.annotation.model.ConceptAssociationTemplate;
import org.mbari.m3.vars.annotation.model.ConceptDetails;
import org.slf4j.LoggerFactory;

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

    /** Not all link-templates may need to be displayed in the UI */
    private List<Pattern> templateFilters;
    private static final String CONFIG_KEY = "concept.service.template.filters";

    public ModifyingConceptService(ConceptService conceptService, Config config) {
        this.conceptService = conceptService;

        try {
            List<String> regex = config.getStringList(CONFIG_KEY);
            templateFilters = regex.stream()
                    .map(Pattern::compile)
                    .collect(Collectors.toList());
        }
        catch (Exception e){
            LoggerFactory.getLogger(getClass())
                    .info("Error reading configuration defined for " + CONFIG_KEY, e);
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
