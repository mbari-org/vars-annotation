package org.mbari.m3.vars.annotation.commands;

/**
 * @author Brian Schlining
 * @since 2017-05-17T10:45:00
 */
public class ShowConceptInTreeView {

    private final String name;

    public ShowConceptInTreeView(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
