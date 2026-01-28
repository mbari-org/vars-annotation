package org.mbari.vars.ui.events;

/**
 * @author Brian Schlining
 * @since 2017-07-20T17:03:00
 */
public class UIChangeEvent<A> extends UIEvent<A> {

    public UIChangeEvent(Object changeSource, A refs) {
        super(changeSource, refs);
    }

}
