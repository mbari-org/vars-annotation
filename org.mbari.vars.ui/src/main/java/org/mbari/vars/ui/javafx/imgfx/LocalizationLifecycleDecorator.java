package org.mbari.vars.ui.javafx.imgfx;

import javafx.collections.ListChangeListener;
import javafx.scene.image.ImageView;
import org.mbari.imgfx.AutoscalePaneController;
import org.mbari.imgfx.etc.rx.events.RemoveLocalizationEvent;
import org.mbari.imgfx.imageview.editor.Localizations;

import org.mbari.vars.ui.javafx.imgfx.domain.VarsLocalization;
import org.mbari.vars.ui.javafx.imgfx.events.AddLocalizationEventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class LocalizationLifecycleDecorator {

    private final IFXToolBox toolBox;
    private final Localizations localizations;

    private static final Logger log = LoggerFactory.getLogger(AnnotationLifecycleDecorator.class);


    public LocalizationLifecycleDecorator(IFXToolBox toolBox,
                                        Localizations localizations) {
        this.toolBox = toolBox;
        this.localizations = localizations;
        init();
    }

    private void init() {

        toolBox.getData()
                .getVarsLocalizations()
                .addListener((ListChangeListener<? super VarsLocalization>) c -> {
                    log.debug("VarsLocalizations have been changed");
                    while (c.next()) {
                        if (c.wasAdded()) {
                            c.getAddedSubList().forEach(this::addVarsLocalizationToView);
                        }
                        else if (c.wasRemoved()) {
                            c.getRemoved().forEach(this::removeVarsLocalizationFromView);
                        }
                    }
                });
    }

    private void addVarsLocalizationToView(VarsLocalization vloc) {
        var eventBus = toolBox.getEventBus();
        var match = localizations.getLocalizations()
                .stream()
                .filter(loc -> loc.getUuid().equals(vloc.getLocalization().getUuid()))
                .findFirst();

        // If a localization with the same UUID already exists remove it first
        if (match.isPresent()) {
            eventBus.publish(new RemoveLocalizationEvent(match.get()));
        }
        eventBus.publish(AddLocalizationEventBuilder.build(vloc.getLocalization()));
    }

    private void removeVarsLocalizationFromView(VarsLocalization vloc) {
        var eventBus = toolBox.getEventBus();
        eventBus.publish(new RemoveLocalizationEvent(vloc.getLocalization()));
    }

}
