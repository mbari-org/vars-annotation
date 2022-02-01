package org.mbari.vars.ui.javafx.imgfx;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.image.Image;
import org.mbari.imgfx.etc.rx.events.AddLocalizationEvent;
import org.mbari.imgfx.etc.rx.events.RemoveLocalizationEvent;
import org.mbari.imgfx.imageview.editor.AnnotationPaneController;
import org.mbari.imgfx.roi.Localization;
import org.mbari.vars.ui.javafx.imgfx.domain.VarsLocalization;
import org.mbari.vars.ui.messages.ReloadServicesMsg;

import java.util.ArrayList;
import java.util.List;

public class IFXPaneController {

    private final IFXToolBox toolBox;
    private AnnotationPaneController annotationPaneController;
    private IFXVarsPaneController varsPaneController;
    private AnnotationLifecycleDecorator annotationLifecycleDecorator;
    private LocalizationLifecycleDecorator localizationLifecycleDecorator;

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
                .getConceptService()
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
}
