package org.mbari.m3.vars.annotation.model;

import java.util.List;

/**
 * @author Brian Schlining
 * @since 2017-05-11T15:51:00
 */
public class Concept {
    private String name;
    private String rank;
    private List<Concept> children;
}
