package org.mbari.vars.annotation.ui.javafx.imgfx;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.mbari.vars.services.model.Image;
import org.mbari.vars.annotation.ui.javafx.imgfx.domain.VarsLocalization;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class contains the current images in the annotation application. These images can also
 * be ones without any annotations. It has the following fields:
 *
 * <ul>
 *     <li>images - All images in the currently opened media</li>
 *     <li>selectedImage - The image that is currently selected and being annotated. This may be null</li>
 *     <li>varsLocalization - All the localizations found in the annotations/associations for the currently selected image.</li>
 * </ul>
 */
public class IFXData {

    private final ObservableList<Image> images = FXCollections.observableArrayList();
    private final ObjectProperty<Image> selectedImage = new SimpleObjectProperty<>();
    private final SortedSet<UUID> sortedImageReferenceUuids = Collections.synchronizedSortedSet(new TreeSet<>());
    private final ObservableList<VarsLocalization> varsLocalizations = FXCollections.observableArrayList();



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

//        varsLocalizations.addListener((ListChangeListener<? super VarsLocalization>) );

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

    public ObservableList<VarsLocalization> getVarsLocalizations() {
        return varsLocalizations;
    }
}
