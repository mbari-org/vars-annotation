package org.mbari.vars.ui.javafx.imgfx;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.mbari.vars.services.model.Image;

import java.util.*;
import java.util.stream.Collectors;

public class IFXData {

    private final ObservableList<Image> images = FXCollections.observableArrayList();
    private final ObjectProperty<Image> selectedImage = new SimpleObjectProperty<>();
    private final SortedSet<UUID> sortedImageReferenceUuids = Collections.synchronizedSortedSet(new TreeSet<>());

    public IFXData() {
        init();
    }

    private void init() {
        images.addListener((ListChangeListener<? super Image>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    var added = c.getAddedSubList()
                            .stream()
                            .map(Image::getImageReferenceUuid)
                            .collect(Collectors.toList());
                    sortedImageReferenceUuids.addAll(added);
                }
                if (c.wasRemoved()) {
                    var removed = c.getRemoved()
                            .stream()
                            .map(Image::getImageReferenceUuid)
                            .collect(Collectors.toList());
                    removed.forEach(sortedImageReferenceUuids::remove);
                }
            }
        });
    }

    public Image getSelectedImage() {
        return selectedImage.get();
    }

    public ObjectProperty<Image> selectedImageProperty() {
        return selectedImage;
    }

    public void setSelectedImage(Image selectedImage) {
        this.selectedImage.set(selectedImage);
    }

    public ObservableList<Image> getImages() {
        return images;
    }

    /**
     * This is a collection of imageReferenceUuids for all images listed in the data
     * object. It is synced automatically with the image collection
     * @return
     */
    public SortedSet<UUID> getSortedImageReferenceUuids() {
        return Collections.unmodifiableSortedSet(sortedImageReferenceUuids);
    }



}
