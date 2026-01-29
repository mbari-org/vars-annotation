package org.mbari.vars.annotation.ui.javafx.imgfx;

import javafx.application.Platform;
import javafx.scene.image.Image;
import org.mbari.imgfx.demos.imageview.editor.AnnotationPaneController;
import org.mbari.imgfx.etc.rx.events.RemoveLocalizationEvent;
import org.mbari.vars.annotation.ui.javafx.imgfx.domain.VarsLocalization;
import org.mbari.vars.annotation.ui.javafx.imgfx.events.AddLocalizationEventBuilder;
import org.mbari.vars.annotation.ui.javafx.imgfx.events.DrawVarsLocalizationEvent;
import org.mbari.vars.annotation.ui.messages.ReloadServicesMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class IFXPaneController {

    private final IFXToolBox toolBox;
    private AnnotationPaneController annotationPaneController;
    private IFXVarsPaneController varsPaneController;
    private AnnotationLifecycleDecorator annotationLifecycleDecorator;
    private LocalizationLifecycleDecorator localizationLifecycleDecorator;

    private static final Logger log = LoggerFactory.getLogger(IFXPaneController.class);

    public IFXPaneController(IFXToolBox toolBox) {
        this.toolBox = toolBox;
        init();
    }

    private void init() {
        annotationPaneController = new AnnotationPaneController(toolBox.getEventBus());
        var autoscalaPane = annotationPaneController.getAutoscalePaneController();
        varsPaneController = IFXVarsPaneController.newInstance(toolBox, autoscalaPane.getAutoscale());
        annotationPaneController.getPane().setRight(varsPaneController.getRoot());
        annotationLifecycleDecorator = new AnnotationLifecycleDecorator(toolBox,
                autoscalaPane,
                annotationPaneController.getLocalizations());
        localizationLifecycleDecorator = new LocalizationLifecycleDecorator(toolBox,
                annotationPaneController.getLocalizations());

        loadConcepts();

        var appEventBus = toolBox.getUIToolBox()
                .getEventBus()
                .toObserverable();

        appEventBus.ofType(ReloadServicesMsg.class)
                .subscribe(msg -> loadConcepts());

        appEventBus.ofType(DrawVarsLocalizationEvent.class)
                .subscribe(event -> addVarsLocalizationToView(event.varsLocalization()));

        toolBox.getData()
                .selectedImageProperty()
                .addListener((obs, oldv, newv) -> setImage(newv));




        // TODO listen to selected annotation and select the correct image (if different than current image)

        // TODO liste to selected image. Get it's annotations with localizations and display them

        // TODO listen to RemoveLocalizatinEvents and delete the relevant assoctiation. Note
        // that thre will no always be a matching association. For example if adding to exsting
        // annotations but no annotation is selected.

    }


    private void loadConcepts() {
        toolBox.getUIToolBox()
                .getServices()
                .conceptService()
                .findAllNames()
                .thenAccept(concepts -> Platform.runLater(() -> annotationPaneController.setConcepts(concepts)));

    }


    private void setImage(org.mbari.vars.services.model.Image image) {
        var jfxImage = image == null ? null : new Image(image.getUrl().toExternalForm());
        annotationPaneController.resetUsingImage(jfxImage);
    }

    public AnnotationPaneController getAnnotationPaneController() {
        return annotationPaneController;
    }

    /**
     * Called when a new image is selected. Just redraws the existing localizations
     * @param vlocs
     */
    private void addVarsLocalizationsToView(Collection<? extends VarsLocalization> vlocs) {
        var eventBus = toolBox.getEventBus();
        vlocs.stream()
                .map(VarsLocalization::getLocalization)
                .map(AddLocalizationEventBuilder::build)
                .forEach(eventBus::publish);
    }

    private void addVarsLocalizationToView(VarsLocalization vloc) {
        var eventBus = toolBox.getEventBus();
        var match = annotationPaneController.getLocalizations()
                .getLocalizations()
                .stream()
                .filter(loc -> loc.getUuid().equals(vloc.getLocalization().getUuid()))
                .findFirst();

        // If a localization with the same UUID already exists remove it first
        if (match.isPresent()) {
            eventBus.publish(new RemoveLocalizationEvent(match.get()));
        }

        // FIXME is this working??
        var event = AddLocalizationEventBuilder.build(vloc.getLocalization());
        log.info("New builder event: " + event);
        eventBus.publish(event);
    }

    private void removeVarsLocalizationFromView(VarsLocalization vloc) {
        var eventBus = toolBox.getEventBus();
        eventBus.publish(new RemoveLocalizationEvent(vloc.getLocalization()));
    }
}
