package org.mbari.vars.ui.javafx.localization;

import org.mbari.vars.core.EventBus;
import org.mbari.vars.ui.events.AnnotationsAddedEvent;
import org.mbari.vars.ui.events.AnnotationsChangedEvent;
import org.mbari.vars.ui.events.AnnotationsRemovedEvent;

/**
 * @author Brian Schlining
 * @since 2020-03-05T17:02:00
 */
public class LocalizationController {
    private final EventBus eventBus;

    public LocalizationController(EventBus eventBus) {
        this.eventBus = eventBus;
        eventBus.toObserverable()
                .ofType(AnnotationsAddedEvent.class)
                .filter(evt -> evt.getEventSource() != this)
                .subscribe(this::handleAdded);
        eventBus.toObserverable()
                .ofType(AnnotationsRemovedEvent.class)
                .filter(evt -> evt.getEventSource() != this)
                .subscribe(this::handleRemoved);
        eventBus.toObserverable()
                .ofType(AnnotationsChangedEvent.class)
                .filter(evt -> evt.getEventSource() != this)
                .subscribe(this::handleChanged);
    }

    public void handleAdded(AnnotationsAddedEvent evt) {

    }

    public void handleRemoved(AnnotationsRemovedEvent evt) {

    }

    public void handleChanged(AnnotationsChangedEvent evt) {

    }
}
