package org.mbari.vars.ui.javafx.imgfx;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.mbari.vars.services.model.Image;

public class IFXData {

    private final ObjectProperty<Image> selectedImage = new SimpleObjectProperty<>();


    public Image getSelectedImage() {
        return selectedImage.get();
    }

    public ObjectProperty<Image> selectedImageProperty() {
        return selectedImage;
    }

    public void setSelectedImage(Image selectedImage) {
        this.selectedImage.set(selectedImage);
    }
}
