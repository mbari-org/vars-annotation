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
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2018-08-10T10:38:00
 */
public class DetachFramegrabCmd implements Command {

    private final Collection<Annotation> originalAnnotations;
    private final Collection<Annotation> modifiedAnnotations = new CopyOnWriteArraySet<>();
    private Collection<Image> originalImages = new CopyOnWriteArrayList<>();

    public DetachFramegrabCmd(Collection<Annotation> originalAnnotations) {
        Preconditions.checkNotNull(originalAnnotations,
                "Can not execute command on empty collection");
        this.originalAnnotations = ImmutableList.copyOf(originalAnnotations);
    }

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

        // Fn to delete an image, them look up related
        Function<Image, CompletableFuture<List<Annotation>>> deleteImageAndFindAnnotationsFn =
                image -> annotationService.deleteImage(image.getImageReferenceUuid())
                .thenCompose(b -> annotationService.findByImageReference(image.getImageReferenceUuid()));

        // Fn to add refreshed annotations to modifiedAnnotations and refresh view
        Consumer<List<Annotation>> updateAnnotationsFn = annos -> {
            Set<UUID> observationUuids = annos.stream()
                    .map(Annotation::getObservationUuid)
                    .collect(Collectors.toSet());
            modifiedAnnotations.addAll(annos);
            decorator.refreshAnnotationsView(observationUuids);
        };

        CompletableFuture<Collection<Image>> f = findImagesToDelete(toolBox);

        f.whenComplete((images, exception) -> {
            if (exception != null) {
                Observable<List<Annotation>> observable = AsyncUtils.observeAll(images, deleteImageAndFindAnnotationsFn);
                observable.subscribe(updateAnnotationsFn, t -> showAlert(t, toolBox));
            }
            else {
                showAlert(exception, toolBox);
            }
        });

    }

    @Override
    public void unapply(UIToolBox toolBox) {
        AnnotationService annotationService = toolBox.getServices().getAnnotationService();
        AnnotationServiceDecorator decorator = new AnnotationServiceDecorator(toolBox);

        Function<Image, CompletableFuture<List<Annotation>>> createImageAndFindAnnotationFn
        originalImages.forEach(annotationService::createImage);
    }

    @Override
    public String getDescription() {
        return null;
    }
}
