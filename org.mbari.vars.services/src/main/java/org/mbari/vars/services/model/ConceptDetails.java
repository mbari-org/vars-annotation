package org.mbari.vars.services.model;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-05-11T15:53:00
 */
public class ConceptDetails {
    private String name;
    private List<String> alternateNames;
    private List<ConceptMedia> media;
    private List<ConceptDescriptor> descriptors;

    public ConceptDetails(String name, List<String> alternateNames, List<ConceptMedia> media, List<ConceptDescriptor> descriptors) {
        this.name = name;
        this.alternateNames = Collections.unmodifiableList(alternateNames.stream()
                .sorted((a, b) -> a.compareToIgnoreCase(b))
                .collect(Collectors.toList()));
        this.media = Collections.unmodifiableList(media);
        this.descriptors = Collections.unmodifiableList(descriptors);
    }

    public String getName() {
        return name;
    }

    public List<String> getAlternateNames() {
        return alternateNames;
    }

    public List<ConceptMedia> getMedia() {
        return media;
    }

    public List<ConceptDescriptor> getDescriptors() {
        return descriptors;
    }
}
