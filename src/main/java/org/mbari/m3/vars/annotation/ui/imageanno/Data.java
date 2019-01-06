package org.mbari.m3.vars.annotation.ui.imageanno;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.StackPane;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Image;
import org.mbari.m3.vars.annotation.ui.shared.ImageViewExt;

public class Data {

    private final ObjectProperty<Image> image = new SimpleObjectProperty<>();

    private final ObjectProperty<LayerController> layerController = new SimpleObjectProperty<>();

    private final ImageViewExt imageViewExt;

    private final StackPane stackPane;

    public Data(StackPane stackPane, ImageViewExt imageViewExt) {
        this.stackPane = stackPane;
        this.imageViewExt = imageViewExt;
    }

    public ImageViewExt getImageViewExt() {
        return imageViewExt;
    }

    public StackPane getStackPane() {
        return stackPane;
    }

    public Image getImage() {
        return image.get();
    }

    public ObjectProperty<Image> imageProperty() {
        return image;
    }

    public void setImage(Image image) {
        this.image.set(image);
    }

    public LayerController getLayerController() {
        return layerController.get();
    }

    public ObjectProperty<LayerController> layerControllerProperty() {
        return layerController;
    }

    public void setLayerController(LayerController layerController) {
        this.layerController.set(layerController);
    }


}
