package org.mbari.m3.vars.annotation.commands;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.messages.ShowFatalErrorAlert;
import org.mbari.m3.vars.annotation.messages.ShowNonfatalErrorAlert;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Image;
import org.mbari.m3.vars.annotation.model.ImageReference;
import org.mbari.m3.vars.annotation.services.AnnotationService;
import org.mbari.m3.vars.annotation.ui.AnnotationServiceDecorator;
import org.mbari.m3.vars.annotation.util.AsyncUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2018-08-10T10:38:00
 */
public class DetachFramegrabCmd implements Command {

    private final Collection<Annotation> originalAnnotations;
    private Collection<Image> originalImages = new CopyOnWriteArrayList<>();

    public DetachFramegrabCmd(Collection<Annotation> originalAnnotations) {
        Preconditions.checkNotNull(originalAnnotations,
                "Can not execute command on empty collection");
        this.originalAnnotations = ImmutableList.copyOf(originalAnnotations);
    }

    /**
     * Looks up all the images that need to be deleted from the annotations.
     * The results are cached so that we don't need to look them up again through
     * subseguent apply/unapply cycles.
     * @param toolBox
     * @return The images to delete
     */
    private CompletableFuture<Collection<Image>> findImagesToDelete(UIToolBox toolBox) {

        AnnotationService annotationService = toolBox.getServices().getAnnotationService();

        // Can't delete until we cache the images we're deleting. Store them in this future
        CompletableFuture<Collection<Image>> f = new CompletableFuture<>();

        if (!originalAnnotations.isEmpty() && originalImages.isEmpty()) {

            // Extract all distinct image_reference_uuid from annotations
            List<UUID> imageReferenceUuids = originalAnnotations.stream()
                    .flatMap(anno -> anno.getImages().stream())
                    .map(ImageReference::getUuid)
                    .distinct()
                    .collect(Collectors.toList());

            // Get the original images from the database. We'll need them for undo
            CompletableFuture<Collection<Image>> findImagesFuture =
                    AsyncUtils.collectAll(imageReferenceUuids, annotationService::findImageByUuid);
            findImagesFuture.whenComplete((images, exception) -> {
                if (exception == null) {
                    originalImages.addAll(images);
                    f.complete(originalImages);  // COMPLETED FUTURE!
                }
                else {
                    showAlert(exception, toolBox);
                }
            });

        }
        else {
            f.complete(originalImages);         // COMPLETED FUTURE!
        }
        return f;
    }

    private void showAlert(Throwable t, UIToolBox toolBox) {
        EventBus eventBus = toolBox.getEventBus();
        ResourceBundle i18n = toolBox.getI18nBundle();
        String title = i18n.getString("commands.detachimage.title");
        String header = i18n.getString("commands.detachimage.header");
        if (t instanceof Exception) {
            String content = i18n.getString("commands.detachimage.exception.content");
            eventBus.send(new ShowNonfatalErrorAlert(title, header, content, (Exception) t));
        }
        else {
            String content = i18n.getString("commands.detachimage.error.content");
            eventBus.send(new ShowFatalErrorAlert(title, header, content, new RuntimeException(t)));
        }
    }


    @Override
    public void apply(UIToolBox toolBox) {
        AnnotationService annotationService = toolBox.getServices().getAnnotationService();
        AnnotationServiceDecorator decorator = new AnnotationServiceDecorator(toolBox);


        Function<Image, CompletableFuture> deleteImageFn = image ->
                annotationService.deleteImage(image.getImageReferenceUuid());

        findImagesToDelete(toolBox)
                .thenCompose(images -> decorator.findAnnotationsForImages(images)
                        .thenCompose(annotations -> AsyncUtils.completeAll(images, deleteImageFn).thenApply(v -> annotations))
                        .thenAccept(annotations -> {
                            Set<UUID> observationUuids = annotations.stream()
                                    .map(Annotation::getObservationUuid)
                                    .collect(Collectors.toSet());
                            decorator.refreshAnnotationsView(observationUuids);
                        })
                );


    }

    @Override
    public void unapply(UIToolBox toolBox) {
        AnnotationService annotationService = toolBox.getServices().getAnnotationService();
        AnnotationServiceDecorator decorator = new AnnotationServiceDecorator(toolBox);

        // Create an iamge and
        Function<Image, CompletableFuture<List<Annotation>>> createImageAndFindAnnotationFn =
                image -> annotationService.createImage(image)
                        .thenCompose(image1 -> annotationService.findByImageReference(image1.getImageReferenceUuid()));


        AsyncUtils.collectAll(originalImages, createImageAndFindAnnotationFn)
                .whenComplete((annotationLists, exception) -> {
                    List<Annotation> annotations = annotationLists.stream()
                            .flatMap(List::stream)
                            .collect(Collectors.toList());
                    if (exception == null) {
                        Set<UUID> observationUuids = annotations.stream()
                                .map(Annotation::getObservationUuid)
                                .collect(Collectors.toSet());
                        decorator.refreshAnnotationsView(observationUuids);
                    }
                    else {
                        showAlert(exception, toolBox);
                    }
                });
    }

    @Override
    public String getDescription() {
        int size = originalAnnotations == null ? 0 : originalAnnotations.size();
        return "Detach images from " + size + " annotations";
    }
}
