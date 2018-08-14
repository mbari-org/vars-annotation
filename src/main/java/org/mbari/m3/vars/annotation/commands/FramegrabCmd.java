package org.mbari.m3.vars.annotation.commands;

import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.events.AnnotationsAddedEvent;
import org.mbari.m3.vars.annotation.events.AnnotationsRemovedEvent;
import org.mbari.m3.vars.annotation.events.AnnotationsSelectedEvent;
import org.mbari.m3.vars.annotation.mediaplayers.MediaPlayer;
import org.mbari.m3.vars.annotation.messages.ShowAlert;
import org.mbari.m3.vars.annotation.messages.ShowWarningAlert;
import org.mbari.m3.vars.annotation.model.*;
import org.mbari.m3.vars.annotation.services.AnnotationService;
import org.mbari.m3.vars.annotation.services.ConceptService;
import org.mbari.m3.vars.annotation.services.ImageCaptureService;
import org.mbari.m3.vars.annotation.ui.ImageArchiveServiceDecorator;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.VideoState;

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author Brian Schlining
 * @since 2018-08-13T11:41:00
 */
public class FramegrabCmd implements Command {


    private volatile Annotation annotationRef;
    private volatile Image pngImageRef;
    private volatile Image jpgImageRef;

    private class CreatedData {
        final Annotation annotation;
        final Image image;

        public CreatedData(Annotation annotation, Image image) {
            this.annotation = annotation;
            this.image = image;
        }
    }

    private class ArchiveResults {
        final ImageUploadResults imageUploadResults;
        final CreatedData createdData;

        public ArchiveResults(ImageUploadResults imageUploadResults, CreatedData createdData) {
            this.imageUploadResults = imageUploadResults;
            this.createdData = createdData;
        }
    }

    @Override
    public void apply(UIToolBox toolBox) {
        if (annotationRef != null && pngImageRef != null) {
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
        AnnotationService annotationService = toolBox.getServices().getAnnotationService();
        ImageArchiveServiceDecorator decorator = new ImageArchiveServiceDecorator(toolBox);
        EventBus eventBus = toolBox.getEventBus();
        List<CompletableFuture> futures = new ArrayList<>();
        if (annotationRef != null) {
            // Delete annotation and notify UI
            CompletableFuture cf = annotationService.deleteAnnotation(annotationRef.getObservationUuid())
                    .thenAccept(b -> eventBus.send(new AnnotationsRemovedEvent(annotationRef)));
            futures.add(cf);
        }
        if (pngImageRef != null) {
            futures.add(annotationService.deleteImage(pngImageRef.getImageReferenceUuid()));
        }
        if (jpgImageRef != null) {
            futures.add(annotationService.deleteImage(jpgImageRef.getImageReferenceUuid()));
        }
        CompletableFuture[] futuresArray = futures.toArray(new CompletableFuture[futures.size()]);
        CompletableFuture.allOf(futuresArray)
                .thenAccept(v -> {
                    // The png is the first one created so it HAS to be present for the others to exist
                    // We only need this one to find all the annotations that were affected
                    if (pngImageRef != null) {
                        decorator.refreshRelatedAnnotations(pngImageRef.getImageReferenceUuid());
                    }
                });
    }

    @Override
    public String getDescription() {
        return null;
    }

    private void applyFromCachedData(UIToolBox toolBox) {

        if (annotationRef != null && pngImageRef != null) {
            AnnotationService annotationService  = toolBox.getServices().getAnnotationService();
            ImageArchiveServiceDecorator decorator = new ImageArchiveServiceDecorator(toolBox);
            List<CompletableFuture> futures = new ArrayList<>();
            futures.add(annotationService.createAnnotation(annotationRef));
            futures.add(annotationService.createImage(pngImageRef));
            if (jpgImageRef != null) {
                futures.add(annotationService.createImage(jpgImageRef));
            }
            CompletableFuture[] futureArray = futures.toArray(new CompletableFuture[futures.size()]);
            CompletableFuture.allOf(futureArray)
                    .thenAccept(v -> decorator.refreshRelatedAnnotations(pngImageRef.getImageReferenceUuid()));
        }

    }

    private void lookupDataAndApply(UIToolBox toolBox,
                                    Media media,
                                    MediaPlayer<? extends VideoState, ? extends VideoError> mediaPlayer) {


        // -- Capture image
        File imageFile = ImageArchiveServiceDecorator.buildLocalImageFile(media, ".png");
        Optional<Framegrab> framegrabOpt = capture(imageFile, media, mediaPlayer);

        if (!framegrabOpt.isPresent()) {
            ResourceBundle i18n = toolBox.getI18nBundle();
            String content = i18n.getString("commands.framecapture.nomedia.content") +
                    imageFile.getAbsolutePath();
            showWarningAlert(toolBox,  content);
        }
        else {

            Framegrab framegrab = framegrabOpt.get();
            ImageArchiveServiceDecorator decorator = new ImageArchiveServiceDecorator(toolBox);
            // -- 1. Upload image to server and register in annotation service
            decorator.createImageFromExistingImagePath(media, framegrab, imageFile.toPath())
                    .thenCompose(pngOpt -> {
                        if (pngOpt.isPresent()) {
                            CreatedImageData createdImageData = pngOpt.get();
                            pngImageRef = createdImageData.getImage();
                            // -- 2. Create an annotation at the same index as the image
                            return createAnnotationInDatastore(toolBox, pngImageRef.getVideoIndex()).thenCompose(annotation -> {
                                annotationRef = annotation;
                                EventBus eventBus = toolBox.getEventBus();
                                eventBus.send(new AnnotationsAddedEvent(annotationRef));
                                eventBus.send(new AnnotationsSelectedEvent(annotationRef));
                                // -- 3. Create a jpeg
                                return decorator.createdCompressedFramegrab(media,
                                        framegrab,
                                        createdImageData.getImageUploadResults())
                                        .thenApply(jpgOpt -> {
                                            jpgOpt.ifPresent(cid -> jpgImageRef = cid.getImage());
                                            return jpgOpt;
                                        });

                            });
                        }
                        else {
                            throw new RuntimeException("Failed to capture framgrab");
                        }
                    })
                    .whenComplete((opt, throwable) -> {
                        // refresh whether is succeeds or fails
                        boolean deleteImage = false;
                        if (pngImageRef == null) {
                            showWarningAlert(toolBox, "Framegrab capture failed");
                        }
                        else if (pngImageRef != null && annotationRef == null) {
                            showWarningAlert(toolBox, "Failed to create an annotation for the framegrab");
                            deleteImage = true;
                        }
                        decorator.refreshRelatedAnnotations(pngImageRef.getImageReferenceUuid(), deleteImage);
                    });


        }
    }



    private void showWarningAlert(UIToolBox toolBox, String content) {
        ResourceBundle i18n = toolBox.getI18nBundle();
        String title = i18n.getString("commands.framecapture.title");
        String header = i18n.getString("commands.framecapture.header");
        EventBus eventBus = toolBox.getEventBus();
        ShowAlert alert = new ShowWarningAlert(title, header, content);
        eventBus.send(alert);
    }

    private static Optional<Framegrab> capture(File imageFile,
                                               Media media,
                                               MediaPlayer<? extends VideoState, ? extends VideoError> mediaPlayer) {
        try {
            ImageCaptureService service = mediaPlayer.getImageCaptureService();
            Framegrab framegrab = service.capture(imageFile);

            // If there's an elapsed time, make sure the recordedTimestamp is
            // set and correct
            framegrab.getVideoIndex().ifPresent(videoIndex ->
                videoIndex.getElapsedTime().ifPresent(elapsedTime -> {
                    Instant recordedDate = media.getStartTimestamp().plus(elapsedTime);
                    framegrab.setVideoIndex(new VideoIndex(elapsedTime, recordedDate));
                }));
            return Optional.of(framegrab);
        }
        catch (Exception e) {
            // TODO show error
            return Optional.empty();
        }
    }

    private CompletableFuture<Annotation> createAnnotationInDatastore(UIToolBox toolBox, VideoIndex videoIndex) {
        AnnotationService annotationService = toolBox.getServices().getAnnotationService();
        ConceptService conceptService = toolBox.getServices().getConceptService();

        return conceptService.findRoot()
                .thenCompose(root -> {
                    // Insert Annotation in database
                    Annotation annotation = CommandUtil.buildAnnotation(toolBox.getData(),
                            root.getName(), videoIndex);
                   return annotationService.createAnnotation(annotation);
                });

    }


}
