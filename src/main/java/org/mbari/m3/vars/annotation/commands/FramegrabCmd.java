package org.mbari.m3.vars.annotation.commands;

import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.events.AnnotationsAddedEvent;
import org.mbari.m3.vars.annotation.events.AnnotationsRemovedEvent;
import org.mbari.m3.vars.annotation.events.AnnotationsSelectedEvent;
import org.mbari.m3.vars.annotation.mediaplayers.MediaPlayer;
import org.mbari.m3.vars.annotation.messages.ShowWarningAlert;
import org.mbari.m3.vars.annotation.model.*;
import org.mbari.m3.vars.annotation.services.AnnotationService;
import org.mbari.m3.vars.annotation.services.ImageArchiveServiceDecorator;
import org.mbari.m3.vars.annotation.services.ImageCaptureService;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.VideoState;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author Brian Schlining
 * @since 2017-08-31T14:43:00
 */
public class FramegrabCmd implements Command {

    private static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'")
            .withZone(ZoneOffset.UTC);

    private volatile Annotation annotationRef;
    private volatile Image imageRef;

    @Override
    public void apply(UIToolBox toolBox) {

        if (annotationRef != null && imageRef != null) {
            skipTheBS(toolBox);
            return;
        }

        ResourceBundle i18n = toolBox.getI18nBundle();

        Media media = toolBox.getData().getMedia();
        if (media == null) {
            toolBox.getEventBus()
                    .send(new ShowWarningAlert(i18n.getString("commands.framecapture.title"),
                            i18n.getString("commands.framecapture.header"),
                            i18n.getString("commands.framecapture.nomedia.content")));
            return;
        }

        MediaPlayer<? extends VideoState, ? extends VideoError> mediaPlayer = toolBox.getMediaPlayer();
        if (mediaPlayer == null) {
            toolBox.getEventBus()
                    .send(new ShowWarningAlert(i18n.getString("commands.framecapture.title"),
                            i18n.getString("commands.framecapture.header"),
                            i18n.getString("commands.framecapture.nomediaplayer.content")));
            return;
        }

        // -- Capture image
        File imageFile = ImageArchiveServiceDecorator.buildLocalImageFile(media, ".png");
        Optional<Framegrab> framegrabOpt = capture(imageFile, media, mediaPlayer);
        if (!framegrabOpt.isPresent()) {
            // TODO handle error. Show warning
            return;
        }

        URL url;
        try {
            url = imageFile.toURI().toURL();
        } catch (MalformedURLException e) {
            // TODO handle error. Show warning
            return;
        }

        EventBus eventBus = toolBox.getEventBus();
        Framegrab framegrab = framegrabOpt.get();
        ImageArchiveServiceDecorator.createImageInDatastore(toolBox, media, framegrab, url).thenAccept(image ->
           createAnnotationInDatastore(toolBox, image)
                   .thenApply(a1 -> {
                       // notify UI
                       eventBus.send(new AnnotationsAddedEvent(a1));
                       eventBus.send(new AnnotationsSelectedEvent(a1));
                       return a1;
                   })
                  .thenApply(a1 -> {
                    // upload image
                    archiveImage(toolBox, media, image, imageFile.toPath()).thenAccept(iur -> {
                        // update URL used by images in annotations
                        updateAnnotationWithArchivedImage(toolBox, image, iur)
                                .thenAccept(annos -> {
                                    // Notify UI
                                    List<Annotation> selectedAnnotations =
                                            new ArrayList<>(toolBox.getData().getSelectedAnnotations());
                                    eventBus.send(new AnnotationsRemovedEvent(annos));
                                    eventBus.send(new AnnotationsAddedEvent(annos));
                                    eventBus.send(new AnnotationsSelectedEvent(selectedAnnotations));
                                })
                               .thenAccept(v -> imageFile.delete())
                                .thenAccept(v -> {
                                    ImageArchiveServiceDecorator decorator = new ImageArchiveServiceDecorator(toolBox);
                                    decorator.compressFramegrab(media, framegrab, iur);
                                });
                    });
                    return  null;
                }));

    }

    @Override
    public void unapply(UIToolBox toolBox) {
        AnnotationService annotationService = toolBox.getServices().getAnnotationService();
        EventBus eventBus = toolBox.getEventBus();
        if (annotationRef != null && imageRef != null) {
            // Delete annotations
            // Find ones that share the same image ref
            // Delete image ref
            // Republish other annotations so UI updates
            annotationService.deleteAnnotation(annotationRef.getObservationUuid())
                    .thenAccept(v -> ImageArchiveServiceDecorator.refreshRelatedAnnotations(toolBox, imageRef.getImageReferenceUuid(), true));

        }
    }

    @Override
    public String getDescription() {
        return null;
    }



    private void skipTheBS(UIToolBox toolBox) {
        AnnotationService annotationService  = toolBox.getServices().getAnnotationService();
        annotationService.createImage(imageRef)
                .thenAccept(img -> annotationService.createAnnotation(annotationRef)
                        .thenAccept(a -> ImageArchiveServiceDecorator.refreshRelatedAnnotations(toolBox, img.getImageReferenceUuid(), false)));
    }

    private CompletableFuture<Annotation> createAnnotationInDatastore(UIToolBox toolBox, Image image) {
        CompletableFuture<Annotation> f0 = new CompletableFuture<>();
        AnnotationService annotationService = toolBox.getServices().getAnnotationService();
        VideoIndex videoIndex = image.getVideoIndex();
        toolBox.getServices()
                .getConceptService()
                .findRoot()
                .thenAccept(root -> {
                    Annotation a0 = CommandUtil.buildAnnotation(toolBox.getData(),
                            root.getName(), videoIndex);
                    annotationService.createAnnotation(a0)
                            .thenAccept(a -> {
                                annotationRef = a;
                                f0.complete(a);
                            });

                });
        return f0;
    }


    public Optional<Framegrab> capture(File imageFile, Media media, MediaPlayer<? extends VideoState, ? extends VideoError> mediaPlayer) {
        try {
            ImageCaptureService service = mediaPlayer
                    .getImageCaptureService();

            Framegrab framegrab0 = service.capture(imageFile);
            framegrab0.getVideoIndex().ifPresent(videoIndex -> {
                videoIndex.getElapsedTime().ifPresent(elapsedTime -> {
                    Instant recordedDate = media.getStartTimestamp().plus(elapsedTime);
                    framegrab0.setVideoIndex(new VideoIndex(elapsedTime, recordedDate));
                });
            });
            return Optional.of(framegrab0);
        }
        catch (Exception e) {
            // TODO show error
            return Optional.empty();
        }
    }

    private CompletableFuture<List<Annotation>> updateAnnotationWithArchivedImage(UIToolBox toolBox,
                                                                                  Image image,
                                                                                  ImageUploadResults imageUploadResults) {
        CompletableFuture<List<Annotation>> cf = new CompletableFuture<>();
        try {
            AnnotationService annotationService = toolBox.getServices().getAnnotationService();
            image.setUrl(imageUploadResults.getUri().toURL());
            annotationService.updateImage(image)
                    .thenAccept(img -> {
                        imageRef = img;
                        annotationService.findByImageReference(image.getImageReferenceUuid())
                            .thenAccept(cf::complete);
                    });
        }
        catch (MalformedURLException e) {
            cf.completeExceptionally(e);
        }

        return cf;

    }


    private CompletableFuture<ImageUploadResults> archiveImage(UIToolBox toolBox, Media media, Image image, Path imagePath) {
        String name = ImageArchiveServiceDecorator.buildName(image.getVideoReferenceUuid(), image.getVideoIndex(), ".png");
        String deploymentId = CommandUtil.getDeploymentId(media);
        return toolBox.getServices()
                .getImageArchiveService()
                .upload(media.getCameraId(),
                        deploymentId,
                        name,
                        imagePath);
    }




}
