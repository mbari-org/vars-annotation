package org.mbari.vars.ui.javafx.imgfx;

import javafx.application.Platform;
import javafx.scene.image.Image;
import org.mbari.imgfx.imageview.editor.AnnotationPaneController;
import org.mbari.vars.ui.messages.ReloadServicesMsg;

public class IFXPaneController {

    private final IFXToolBox toolBox;
    private AnnotationPaneController annotationPaneController;
    private IFXVarsPaneController varsPaneController;

    public IFXPaneController(IFXToolBox toolBox) {
        this.toolBox = toolBox;
        init();
    }

    private void init() {
        annotationPaneController = new AnnotationPaneController(toolBox.getEventBus());
        varsPaneController = IFXVarsPaneController.newInstance(toolBox, annotationPaneController.getAutoscalePaneController().getAutoscale());
        annotationPaneController.getPane().setRight(varsPaneController.getRoot());
        loadConcepts();

        var appEventBus = toolBox.getUIToolBox()
                .getEventBus()
                .toObserverable();

        appEventBus.ofType(ReloadServicesMsg.class)
                .subscribe(msg -> loadConcepts());

        toolBox.getData()
                .selectedImageProperty()
                .addListener((obs, oldv, newv) -> setImage(newv));

        // TODO listen to selected image. Add listeners to all labels so that when they
        // change an updated annotation command is published.

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


//
//        annotationPaneController.getAutoscalePaneController()
//                .getView()
//                .setImage(jfxImage);
    }

    public AnnotationPaneController getAnnotationPaneController() {
        return annotationPaneController;
    }
}
