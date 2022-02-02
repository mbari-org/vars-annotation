package org.mbari.vars.ui.javafx.imgfx;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import org.mbari.imgfx.etc.rx.EventBus;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Image;
import org.mbari.vars.ui.UIToolBox;

import java.util.Collection;
import java.util.Collections;

public class IFXToolBox {



    private final UIToolBox toolBox;
    private final IFXData data;
    /** URL to the stylesheet used for the apps */
    private final Collection<String> stylesheets;
    private final EventBus eventBus;

    private ObjectProperty<Color> editedColor = new SimpleObjectProperty<>();


    private final BooleanProperty active = new SimpleBooleanProperty();


    public IFXToolBox(UIToolBox toolBox,
                      IFXData data,
                      EventBus eventBus,
                      Collection<String> stylesheets) {
        this.toolBox = toolBox;
        this.data = data;
        this.eventBus = eventBus;
        this.stylesheets = Collections.unmodifiableCollection(stylesheets);

//        this.dataDecorator = new ImageLifecycleDecorator(this);

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

    public Color getEditedColor() {
        return editedColor.get();
    }

    public ObjectProperty<Color> editedColorProperty() {
        return editedColor;
    }

    /**
     * @return A readonly ObservableList of annotations that have the
     * same imagedMomentUuid as the selected image.
     */
    public ObservableList<Annotation> getAnnotationsForImage(Image image) {
        if (image == null) {
            return FXCollections.emptyObservableList();
        }
        else {
            var imagedMomentUuid = image.getImagedMomentUuid();
            return getUIToolBox()
                    .getData()
                    .getAnnotations()
                    .filtered(a -> a.getImagedMomentUuid().equals(imagedMomentUuid));
        }


    }



}
