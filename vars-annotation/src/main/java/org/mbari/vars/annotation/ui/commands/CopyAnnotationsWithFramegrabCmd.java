package org.mbari.vars.annotation.ui.commands;

import org.mbari.vars.annosaurus.sdk.r1.AnnotationService;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annosaurus.sdk.r1.models.Image;
import org.mbari.vars.annotation.etc.jdk.Loggers;
import org.mbari.vars.annotation.etc.rxjava.EventBus;
import org.mbari.vars.annotation.model.CreatedImageData;
import org.mbari.vars.annotation.model.ImageData;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.events.AnnotationsAddedEvent;
import org.mbari.vars.annotation.ui.events.AnnotationsRemovedEvent;
import org.mbari.vars.annotation.ui.events.AnnotationsSelectedEvent;
import org.mbari.vars.annotation.ui.javafx.ImageArchiveServiceDecorator;
import org.mbari.vars.annotation.ui.mediaplayers.MediaPlayer;
import org.mbari.vars.annotation.ui.messages.ShowAlert;
import org.mbari.vars.annotation.ui.messages.ShowExceptionAlert;
import org.mbari.vars.annotation.ui.messages.ShowWarningAlert;
import org.mbari.vars.annotation.ui.services.FrameCaptureException;
import org.mbari.vars.annotation.ui.services.FrameCaptureService;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.VideoState;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Combines {@link CopyAnnotationsCmd} and {@link FramegrabCmd}. A framegrab is captured
 * and the selected annotations are copied to the video index of the captured frame so
 * that the copies are attached to the new image.
 *
 * @author Brian Schlining
 * @since 2026-10-08
 */
public class CopyAnnotationsWithFramegrabCmd implements Command {

    private final List<Annotation> originalAnnotations;
    private final String observer;
    private final String activity;
    private final List<Annotation> copiedAnnotations = new ArrayList<>();
    private volatile Image pngImageRef;
    private volatile Image jpgImageRef;
    private static final Loggers log = new Loggers(CopyAnnotationsWithFramegrabCmd.class);

    public CopyAnnotationsWithFramegrabCmd(String observer,
                                           String activity,
                                           Collection<Annotation> originalAnnotations) {
        this.observer = observer;
        this.activity = activity;
        this.originalAnnotations = new ArrayList<>(originalAnnotations);
    }

    @Override
    public void apply(UIToolBox toolBox) {
        if (!copiedAnnotations.isEmpty() && pngImageRef != null) {
            applyFromCachedData(toolBox);
        }
        else {
            Media media = toolBox.getData().getMedia();
            ResourceBundle i18n = toolBox.getI18nBundle();

            if (media == null) {
                String content = i18n.getString("commands.framecapture.nomedia.content");
                showWarningAlert(toolBox, content);
                return;
            }

            MediaPlayer<? extends VideoState, ? extends VideoError> mediaPlayer = toolBox.getMediaPlayer();
            if (mediaPlayer == null) {
                String content = i18n.getString("commands.framecapture.nomediaplayer.content");
                showWarningAlert(toolBox, content);
                return;
            }

            lookupDataAndApply(toolBox, media, mediaPlayer);
        }
    }

    @Override
    public void unapply(UIToolBox toolBox) {
        AnnotationService annotationService = toolBox.getServices().annotationService();
        ImageArchiveServiceDecorator decorator = new ImageArchiveServiceDecorator(toolBox);
        EventBus eventBus = toolBox.getEventBus();
        List<CompletableFuture<?>> futures = new ArrayList<>();
        if (!copiedAnnotations.isEmpty()) {
            List<Annotation> removed = new ArrayList<>(copiedAnnotations);
            List<UUID> uuids = removed.stream()
                    .map(Annotation::getObservationUuid)
                    .collect(Collectors.toList());
            futures.add(annotationService.deleteAnnotations(uuids)
                    .thenAccept(b -> eventBus.send(new AnnotationsRemovedEvent(removed))));
        }
        if (pngImageRef != null) {
            futures.add(annotationService.deleteImage(pngImageRef.getImageReferenceUuid()));
        }
        if (jpgImageRef != null) {
            futures.add(annotationService.deleteImage(jpgImageRef.getImageReferenceUuid()));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenAccept(v -> {
                    if (pngImageRef != null) {
                        decorator.refreshRelatedAnnotations(pngImageRef.getImageReferenceUuid());
                    }
                });
    }

    @Override
    public String getDescription() {
        return "Copy " + originalAnnotations.size() + " annotations with framegrab";
    }

    private void applyFromCachedData(UIToolBox toolBox) {
        AnnotationService annotationService = toolBox.getServices().annotationService();
        ImageArchiveServiceDecorator decorator = new ImageArchiveServiceDecorator(toolBox);
        EventBus eventBus = toolBox.getEventBus();

        // Create the png first so the imaged moment exists before the annotations are added
        annotationService.createImage(pngImageRef)
                .thenCompose(png -> annotationService.createAnnotations(copiedAnnotations))
                .thenCompose(annos -> {
                    copiedAnnotations.clear();
                    copiedAnnotations.addAll(annos);
                    eventBus.send(new AnnotationsAddedEvent(annos));
                    return jpgImageRef == null
                            ? CompletableFuture.completedFuture(null)
                            : annotationService.createImage(jpgImageRef);
                })
                .thenAccept(v -> decorator.refreshRelatedAnnotations(pngImageRef.getImageReferenceUuid()));
    }

    private void lookupDataAndApply(UIToolBox toolBox,
                                    Media media,
                                    MediaPlayer<? extends VideoState, ? extends VideoError> mediaPlayer) {

        // -- Capture image
        File imageFile = ImageArchiveServiceDecorator.buildLocalImageFile(media, ".png");
        ImageData imageData;
        try {
            imageData = FrameCaptureService.capture(imageFile, media, mediaPlayer);
        }
        catch (FrameCaptureException e) {
            showWarningAlert(toolBox, e.getMessage(), e);
            return;
        }

        log.atInfo().log(() -> "Captured image at " + imageData.getVideoIndex().getTimestamp().orElse(null));

        ImageArchiveServiceDecorator decorator = new ImageArchiveServiceDecorator(toolBox);
        // -- 1. Upload image to server and register in annotation service
        decorator.createImageFromExistingImageData(media, imageData, ImageArchiveServiceDecorator.ImageTypes.PNG)
                .thenCompose(pngOpt -> {
                    if (pngOpt.isEmpty()) {
                        throw new RuntimeException("The image archive did not return the saved image");
                    }
                    CreatedImageData createdImageData = pngOpt.get();
                    pngImageRef = createdImageData.getImage();
                    // -- 2. Copy the annotations to the same index as the image
                    return copyAnnotationsInDatastore(toolBox, media, pngImageRef.getVideoIndex())
                            .thenCompose(annos -> {
                                copiedAnnotations.addAll(annos);
                                EventBus eventBus = toolBox.getEventBus();
                                eventBus.send(new AnnotationsAddedEvent(annos));
                                eventBus.send(new AnnotationsSelectedEvent(annos));
                                // -- 3. Create a jpeg
                                return decorator.createJpegWithOverlay(media,
                                                imageData,
                                                createdImageData.getImageUploadResults())
                                        .thenApply(jpgOpt -> {
                                            jpgOpt.ifPresent(cid -> jpgImageRef = cid.getImage());
                                            return jpgOpt;
                                        });
                            });
                })
                .whenComplete((opt, throwable) -> {
                    ResourceBundle i18n = toolBox.getI18nBundle();
                    if (pngImageRef == null) {
                        String msg = FrameCaptureService.withCause(i18n.getString("commands.framecapture.fail.noimage"), throwable);
                        showWarningAlert(toolBox, msg, throwable);
                    }
                    else {
                        boolean deleteImage = copiedAnnotations.isEmpty();
                        if (deleteImage) {
                            String msg = withCause(i18n.getString("commands.framecapture.fail.noannotation"), throwable);
                            showWarningAlert(toolBox, msg, throwable);
                        }
                        decorator.refreshRelatedAnnotations(pngImageRef.getImageReferenceUuid(), deleteImage);
                    }
                });
    }

    private CompletableFuture<Collection<Annotation>> copyAnnotationsInDatastore(UIToolBox toolBox,
                                                                                 Media media,
                                                                                 VideoIndex videoIndex) {
        List<Annotation> copies = originalAnnotations.stream()
                .map(a -> CopyAnnotationsCmd.makeCopy(a, media.getVideoReferenceUuid(), videoIndex, observer, activity))
                .collect(Collectors.toList());

        // Same as CopyAnnotationsCmd: derive recordedTimestamp from the media start time
        if (media.getStartTimestamp() != null) {
            copies.forEach(annotation -> {
                Duration elapsedTime = annotation.getElapsedTime();
                if (elapsedTime != null) {
                    Instant recordedDate = media.getStartTimestamp().plus(elapsedTime);
                    annotation.setRecordedTimestamp(recordedDate);
                }
            });
        }

        return toolBox.getServices()
                .annotationService()
                .createAnnotations(copies);
    }


    private void showWarningAlert(UIToolBox toolBox, String content) {
        showWarningAlert(toolBox, content, null);
    }

    private void showWarningAlert(UIToolBox toolBox, String content, Throwable throwable) {
        ResourceBundle i18n = toolBox.getI18nBundle();
        String title = i18n.getString("commands.framecapture.title");
        String header = i18n.getString("commands.framecapture.header");
        EventBus eventBus = toolBox.getEventBus();

        ShowAlert alert = (throwable == null) ?
                new ShowWarningAlert(title, header, content) :
                new ShowExceptionAlert(title, header, content, new RuntimeException(content, throwable));
        eventBus.send(alert);
    }
}
