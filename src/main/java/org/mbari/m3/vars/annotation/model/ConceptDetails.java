package org.mbari.m3.vars.annotation.model;

import java.net.URL;
import java.util.List;

/**
 * @author Brian Schlining
 * @since 2017-05-11T15:53:00
 */
public class ConceptDetails {
    private String name;
    private List<String> alternateNames;
    private List<ConceptMedia> media;
    private List<ConceptDescriptor> descriptors;
}
