package org.mbari.vars.ui.messages;


/**
 * @author Brian Schlining
 * @since 2017-07-20T17:17:00
 */
public class ShowConcurrentAnnotationsMsg implements Message {

    private final Boolean show;

    public ShowConcurrentAnnotationsMsg(Boolean show) {
        this.show = show;
    }

    public Boolean getShow() {
        return show;
    }
}
