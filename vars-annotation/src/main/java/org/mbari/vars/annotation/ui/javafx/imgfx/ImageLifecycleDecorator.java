package org.mbari.vars.annotation.ui.javafx.imgfx;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.mbari.imgfx.etc.rx.events.RemoveLocalizationEvent;
import org.mbari.vars.annosaurus.sdk.r1.models.Image;
import org.mbari.vars.annosaurus.sdk.r1.models.ImageReference;
import org.mbari.vars.annotation.etc.rxjava.AsyncUtils;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annotation.ui.events.MediaChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This decorator watches for when annotations are added/removed to the Data object in VARS.
 * When annotations change update our data images collection with new or removed images from
 * the VARS side of annotation.
 */
public class ImageLifecycleDecorator {

    private static final Logger log = LoggerFactory.getLogger(ImageLifecycleDecorator.class);
    private final IFXToolBox toolBox;

    /**
     * To make things fast we track which annotations are deleted in IFX, those dont' require any
     * special handling. If an anno is dropped that's not in this list, we need to look up it.s
     * image references to see if they still exist.
     */
    private SortedSet<UUID> droppedObservationUuids = Collections.synchronizedSortedSet(new TreeSet<>());
    private ObservableList<Annotation> annotationsForSelectedImage;

    public ImageLifecycleDecorator(IFXToolBox toolBox) {
        this.toolBox = toolBox;
        init();
    }

    private void init() {

        // Clear out dropped ids when a media is changed
        toolBox.getUIToolBox()
                .getEventBus()
                .toObserverable()
                .ofType(MediaChangedEvent.class)
                .subscribe(event -> droppedObservationUuids.clear());

        // Track removals on the IFX side
        toolBox.getEventBus()
                .toObserverable()
                .ofType(RemoveLocalizationEvent.class)
                .subscribe(event -> droppedObservationUuids.add(event.localization().getUuid()));

        toolBox.getUIToolBox()
                .getData()
                .getAnnotations()
                .addListener((ListChangeListener<? super Annotation>) c -> {
                    if (toolBox.isActive()) {
                        while (c.next()) {
                            if (c.wasAdded()) {
                                syncAddedAnnotations(c.getAddedSubList());
                            }
                            if (c.wasRemoved()) {
                                syncDroppedAnnotations(c.getRemoved());
                            }
                        }
                    }
                });


    }



    private void syncDroppedAnnotations(Collection<? extends Annotation> annotations) {
        if (annotations != null && !annotations.isEmpty()) {

            // -- Get observation UUIDs for annotations dropped outside of IFX
            var observationUuids = new ArrayList<>(annotations.stream()
                    .map(Annotation::getObservationUuid)
                    .collect(Collectors.toList()));
            observationUuids.removeAll(droppedObservationUuids);

            if (!observationUuids.isEmpty()) {

                // -- Figure out which images were removed from the VARS database
                var imageReferenceUuids = new ArrayList<>(annotations.stream()
                        .flatMap(a -> a.getImages().stream())
                        .map(i -> i.getUuid())
                        .collect(Collectors.toList()));

                // -- Lookup the image reference UUIDs for the dropped annotations
                findImagesByImageReferenceUuids(imageReferenceUuids)
                        .thenAccept(images -> {
                            var stillAroundUuids = images.stream()
                                    .map(Image::getImageReferenceUuid)
                                    .collect(Collectors.toList());
                            imageReferenceUuids.removeAll(stillAroundUuids);
                            removeImagesByUuids(imageReferenceUuids);
                        });
            }

        }
    }

    private void removeImagesByUuids(List<UUID> imageReferenceUuids) {
        var images = toolBox.getData().getImages();
        Platform.runLater(() ->
                images.removeIf(image -> imageReferenceUuids.contains(image.getImageReferenceUuid())));
    }


    private void syncAddedAnnotations(Collection<? extends Annotation> annotations) {
        var added = newImageReferenceUuidsForAddedAnnotations(annotations);
        if (!added.isEmpty()) {
            findImagesByImageReferenceUuids(added)
                    .thenAccept(newImages -> toolBox.getData().getImages().addAll(newImages));
        }
    }

    private List<UUID> newImageReferenceUuidsForAddedAnnotations(Collection<? extends Annotation> annotations) {
        if (annotations != null && !annotations.isEmpty()) {

            var imageReferenceUuids = annotations.stream()
                    .flatMap(a -> a.getImages().stream())
                    .map(ImageReference::getUuid)
                    .collect(Collectors.toList());

            if (!imageReferenceUuids.isEmpty()) {

                var existingUuids = toolBox.getData()
                        .getSortedImageReferenceUuids();

                return imageReferenceUuids.stream()
                        .filter(uuid -> !existingUuids.contains(uuid))
                        .collect(Collectors.toList());

            }
        }
        return Collections.emptyList();
    }

    private CompletableFuture<Collection<Image>> findImagesByImageReferenceUuids(List<UUID> imageReferenceUuid) {
        Function<UUID, CompletableFuture<Image>> lookup = uuid ->
                toolBox.getUIToolBox()
                        .getServices()
                        .annotationService()
                        .findImageByUuid(uuid);

        return AsyncUtils.collectAll(imageReferenceUuid, lookup);
    }


}
