package org.mbari.vars.ui.commands;

import org.mbari.vars.core.EventBus;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.events.AnnotationsAddedEvent;
import org.mbari.vars.ui.events.AnnotationsRemovedEvent;
import org.mbari.vars.ui.events.AnnotationsSelectedEvent;
import org.mbari.vars.ui.mediaplayers.MediaPlayer;
import org.mbari.vars.ui.messages.ShowAlert;
import org.mbari.vars.ui.messages.ShowExceptionAlert;
import org.mbari.vars.ui.messages.ShowWarningAlert;
import org.mbari.vars.services.model.*;
import org.mbari.vars.services.AnnotationService;
import org.mbari.vars.services.ConceptService;
import org.mbari.vars.services.ImageCaptureService;
import org.mbari.vars.ui.javafx.ImageArchiveServiceDecorator;
import org.mbari.vars.ui.services.FrameCaptureService;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.VideoState;

import java.io.File;
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
    //private static final Logger log = LoggerFactory.getLogger(FramegrabCmd.class);

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
        Optional<ImageData> framegrabOpt = FrameCaptureService.capture(imageFile, media, mediaPlayer);

        if (framegrabOpt.isEmpty()) {
            //log.warn("No framegrab was captured for {} at {}", media.getVideoName(), media.getUri());
            ResourceBundle i18n = toolBox.getI18nBundle();
            String content = i18n.getString("commands.framecapture.nomedia.content") +
                    imageFile.getAbsolutePath();
            showWarningAlert(toolBox,  content);
        }
        else {

            ImageData imageData = framegrabOpt.get();
            //log.info("Captured image at {}", framegrab.getVideoIndex());

            ImageArchiveServiceDecorator decorator = new ImageArchiveServiceDecorator(toolBox);
            // -- 1. Upload image to server and register in annotation service
            decorator.createImageFromExistingImagePath(media, imageData, imageFile.toPath())
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
                                        imageData,
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
                        ResourceBundle i18n = toolBox.getI18nBundle();
                        if (pngImageRef == null) {
                            String msg = i18n.getString("commands.framecapture.fail.noimage");
                            showWarningAlert(toolBox, msg, throwable);
                        }
                        else if (pngImageRef != null && annotationRef == null) {
                            String msg = i18n.getString("commands.framecapture.faile.noannotation");
                            showWarningAlert(toolBox, msg, throwable);
                            deleteImage = true;
                        }
                        decorator.refreshRelatedAnnotations(pngImageRef.getImageReferenceUuid(), deleteImage);
                    });


        }
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
