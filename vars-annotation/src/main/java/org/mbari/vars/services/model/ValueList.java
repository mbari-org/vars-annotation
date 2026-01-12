package org.mbari.vars.services.model;

import java.util.List;

/**
 * @author Brian Schlining
 * @since 2019-11-12T11:11:00
 */
public class ValueList {
    private List<String> values;

    public ValueList(List<String> values) {
        this.values = values;
    }

    public ValueList() {
    }

    public List<String> getValues() {
        return values;
    }
}
