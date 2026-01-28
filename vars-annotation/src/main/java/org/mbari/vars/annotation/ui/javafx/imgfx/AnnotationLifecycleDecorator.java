package org.mbari.vars.ui.javafx.imgfx;



import javafx.scene.image.ImageView;
import org.mbari.imgfx.AutoscalePaneController;
import org.mbari.imgfx.demos.imageview.editor.Localizations;

import org.mbari.vars.ui.javafx.imgfx.events.DrawVarsLocalizationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AnnotationLifecycleDecorator {


    private final IFXToolBox toolBox;
    private final AutoscalePaneController<ImageView> autoscalePaneController;
    private final Localizations localizations;
    private static final Logger log = LoggerFactory.getLogger(AnnotationLifecycleDecorator.class);


    public AnnotationLifecycleDecorator(IFXToolBox toolBox,
                                        AutoscalePaneController<ImageView> autoscalePaneController,
                                        Localizations localizations) {
        this.toolBox = toolBox;
        this.autoscalePaneController = autoscalePaneController;
        this.localizations = localizations;
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
