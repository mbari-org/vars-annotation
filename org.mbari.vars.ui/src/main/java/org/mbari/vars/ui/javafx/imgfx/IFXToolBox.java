package org.mbari.vars.ui.javafx.imgfx;

import org.mbari.imgfx.etc.rx.EventBus;
import org.mbari.vars.ui.UIToolBox;

import java.util.Collection;
import java.util.Collections;

public class IFXToolBox {

    private final UIToolBox toolBox;
    private final IFXData data;
    /** URL to the stylesheet used for the apps */
    private final Collection<String> stylesheets;
    private final EventBus eventBus;


    public IFXToolBox(UIToolBox toolBox,
                      IFXData data,
                      EventBus eventBus,
                      Collection<String> stylesheets) {
        this.toolBox = toolBox;
        this.data = data;
        this.eventBus = eventBus;
        this.stylesheets = Collections.unmodifiableCollection(stylesheets);
    }

    public UIToolBox getToolBox() {
        return toolBox;
    }

    public IFXData getData() {
        return data;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public Collection<String> getStylesheets() {
        return stylesheets;
    }
}
