package org.mbari.vars.ui.javafx.imgfx;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
    private final IFXDataDecorator dataDecorator;

    private final BooleanProperty active = new SimpleBooleanProperty();


    public IFXToolBox(UIToolBox toolBox,
                      IFXData data,
                      EventBus eventBus,
                      Collection<String> stylesheets) {
        this.toolBox = toolBox;
        this.data = data;
        this.eventBus = eventBus;
        this.stylesheets = Collections.unmodifiableCollection(stylesheets);

        this.dataDecorator = new IFXDataDecorator(this);
    }

    public UIToolBox getUIToolBox() {
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

    /**
     * Flag that can be used to disable uneeded data lookup when stage is not active.
     * @return true if the image annotation stage is shown. False if it is hiddent
     */
    public boolean isActive() {
        return active.get();
    }

    public BooleanProperty activeProperty() {
        return active;
    }


}
