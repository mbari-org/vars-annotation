package org.mbari.vars.ui.events;

/**
 * @author Brian Schlining
 * @since 2017-07-20T17:05:00
 */
public class UISelectionEvent<A> extends UIEvent<A> {

    public UISelectionEvent(Object selectionSource, A refs) {
        super(selectionSource, refs);
    }

    public Object getSelectionSource() {
        return eventSource;
    }
}