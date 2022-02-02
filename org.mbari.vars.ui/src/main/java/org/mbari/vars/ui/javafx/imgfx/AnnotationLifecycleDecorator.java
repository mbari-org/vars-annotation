package org.mbari.vars.ui.javafx.imgfx;



import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.ImageView;
import org.mbari.imgfx.AutoscalePaneController;
import org.mbari.imgfx.etc.rx.events.RemoveLocalizationEvent;
import org.mbari.imgfx.imageview.editor.Localizations;

import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.ui.javafx.imgfx.domain.VarsLocalization;
import org.mbari.vars.ui.javafx.imgfx.events.AddLocalizationEventBuilder;
import org.mbari.vars.ui.javafx.imgfx.events.DrawVarsLocalizationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AnnotationLifecycleDecorator {


    private final IFXToolBox toolBox;
    private final AutoscalePaneController<ImageView> autoscalePaneController;
    private static final Logger log = LoggerFactory.getLogger(AnnotationLifecycleDecorator.class);


    public AnnotationLifecycleDecorator(IFXToolBox toolBox,
                                        AutoscalePaneController<ImageView> autoscalePaneController,
                                        Localizations localizations) {
        this.toolBox = toolBox;
        this.autoscalePaneController = autoscalePaneController;
        init();
    }

    private void init() {

        toolBox.getData()
                .selectedImageProperty()
                .addListener((obs, oldv, newv) -> {
                    log.debug("Detected image change to ", newv);
                    toolBox.getData().getVarsLocalizations().clear();
                    if (newv != null) {
                        var locs = LookupUtil.getVarsLocalizationsForImage(toolBox, newv, autoscalePaneController);

                        toolBox.getData().getVarsLocalizations().setAll(locs);
                        locs.stream()
                                .map(DrawVarsLocalizationEvent::new)
                                .forEach(toolBox.getEventBus()::publish);
                    }
                });

    }



}
