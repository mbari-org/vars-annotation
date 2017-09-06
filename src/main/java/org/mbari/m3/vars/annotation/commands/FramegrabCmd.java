package org.mbari.m3.vars.annotation.commands;

import javafx.collections.ObservableList;
import org.mbari.awt.image.ImageUtilities;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.events.AnnotationsAddedEvent;
import org.mbari.m3.vars.annotation.events.AnnotationsRemovedEvent;
import org.mbari.m3.vars.annotation.events.AnnotationsSelectedEvent;
import org.mbari.m3.vars.annotation.mediaplayers.MediaPlayer;
import org.mbari.m3.vars.annotation.messages.ShowWarningAlert;
import org.mbari.m3.vars.annotation.model.*;
import org.mbari.m3.vars.annotation.services.AnnotationService;
import org.mbari.m3.vars.annotation.services.ImageCaptureService;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.VideoState;
import org.mbari.vcr4j.time.Timecode;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

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
        File imageFile = buildLocalImageFile(media);
        Optional<Framegrab> framegrabOpt = capture(imageFile, media, mediaPlayer);
        if (!framegrabOpt.isPresent()) {
            // TODO handle error. Show warning
            return;
        }

        EventBus eventBus = toolBox.getEventBus();
        Framegrab framegrab = framegrabOpt.get();
        createImageInDatastore(toolBox, media, framegrab, imageFile).thenAccept(image ->
           createAnnotationInDatastore(toolBox, image)
                   .thenApply(a1 -> {
                       // notify UI
                       eventBus.send(new AnnotationsAddedEvent(a1));
                       eventBus.send(new AnnotationsSelectedEvent(a1));
                       return a1;
                   })
                  .thenApply(a1 -> {
                    // upload image
                    archiveImage(media, image, imageFile.toPath(), toolBox).thenAccept(iur -> {
                        // update URL used by images in annotations
                        updateAnnotationWithArchivedImage(toolBox, image, iur)
                                .thenAccept(annos -> {
                                    // Notify UI
                                    ObservableList<Annotation> selectedAnnotations =
                                            toolBox.getData().getSelectedAnnotations();
                                    eventBus.send(new AnnotationsRemovedEvent(annos));
                                    eventBus.send(new AnnotationsAddedEvent(annos));
                                    eventBus.send(new AnnotationsSelectedEvent(selectedAnnotations));
                                })
                               .thenAccept(v -> imageFile.delete());
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
                    .thenAccept(v -> refreshRelatedAnnotations(toolBox, imageRef.getImageReferenceUuid(), true));

        }
    }

    @Override
    public String getDescription() {
        return null;
    }

    private void refreshRelatedAnnotations(UIToolBox toolBox, UUID imageReferenceUuid, boolean deleteImage) {
        AnnotationService annotationService = toolBox.getServices().getAnnotationService();
        EventBus eventBus = toolBox.getEventBus();
        annotationService.findByImageReference(imageRef.getImageReferenceUuid())
                .thenAccept(annotations -> {
                    if (deleteImage) {
                        annotationService.deleteImage(imageRef.getImageReferenceUuid());
                    }
                    if (!annotations.isEmpty()) {
                        List<Annotation> annotations1 = new CopyOnWriteArrayList<>();
                        CompletableFuture[] completableFutures = annotations.stream()
                                .map(a -> annotationService.findByUuid(a.getObservationUuid()).thenApply(annotations1::add))
                                .toArray(CompletableFuture[]::new);
                        CompletableFuture.allOf(completableFutures)
                                .thenAccept(w -> {
                                    // Update any annotations that may have been effected by image deletion
                                    ObservableList<Annotation> selectedAnnotations =
                                            toolBox.getData().getSelectedAnnotations();
                                    eventBus.send(new AnnotationsRemovedEvent(annotations1));
                                    eventBus.send(new AnnotationsAddedEvent(annotations1));
                                    eventBus.send(new AnnotationsSelectedEvent(selectedAnnotations));
                                });
                    }
                });
    }

    private void skipTheBS(UIToolBox toolBox) {
        AnnotationService annotationService  = toolBox.getServices().getAnnotationService();
        annotationService.createImage(imageRef)
                .thenAccept(img -> annotationService.createAnnotation(annotationRef)
                        .thenAccept(a -> refreshRelatedAnnotations(toolBox, img.getImageReferenceUuid(), false)));
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

    private CompletableFuture<Image> createImageInDatastore(UIToolBox toolBox, Media media, Framegrab framegrab, File imageFile) {

        CompletableFuture<Image> cf = new CompletableFuture<>();
        Optional<java.awt.Image> awtImageOpt = framegrab.getImage();
        if (!awtImageOpt.isPresent()) {
            // TODO show alert that framecapture failed
            cf.completeExceptionally(new RuntimeException("No image was captured"));
        }
        else {
            java.awt.Image awtImage = awtImageOpt.get();
            BufferedImage bi = ImageUtilities.toBufferedImage(awtImage);
            Image image = new Image();
            image.setFormat("image/png");
            image.setHeight(bi.getHeight());
            image.setWidth(bi.getWidth());
            image.setVideoReferenceUuid(media.getVideoReferenceUuid());
            image.setDescription("uncompressed image");
            try {
                image.setUrl(imageFile.toURI().toURL());
            } catch (MalformedURLException e) {
                // TODO stop processing. show error
            }

            // If no index use the current timestamp
            VideoIndex videoIndex = framegrab.getVideoIndex().orElse(new VideoIndex(Instant.now()));
            videoIndex.getElapsedTime().ifPresent(image::setElapsedTime);
            videoIndex.getTimecode().ifPresent(image::setTimecode);
            videoIndex.getTimestamp().ifPresent(image::setRecordedTimestamp);

            AnnotationService annotationService = toolBox.getServices().getAnnotationService();
            annotationService.createImage(image)
                    .thenAccept(cf::complete);
        }

        return cf;

    }

    private File buildLocalImageFile(Media media) {
        try {
            String deploymentKey = media.getVideoName() + "-" + media.getVideoReferenceUuid();
            return File.createTempFile(deploymentKey + "-", ".png",
                    Initializer.getImageDirectory().toFile());
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to build a local image temp file", e);
        }
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
                    .thenApply(img -> {
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


    private CompletableFuture<ImageUploadResults> archiveImage(Media media, Image image, Path imagePath, UIToolBox toolBox) {
        String name = buildName(image);
        String deploymentId = CommandUtil.getDeploymentId(media);
        return toolBox.getServices()
                .getImageArchiveService()
                .upload(media.getCameraId(),
                        deploymentId,
                        name,
                        imagePath);
    }


    private String buildName(Image image) {
        // index - uuid
        Optional<Timecode> timecode = Optional.ofNullable(image.getTimecode());
        Optional<Duration> elapsedTime = Optional.ofNullable(image.getElapsedTime());
        Optional<Instant> timestamp = Optional.ofNullable(image.getRecordedTimestamp());
        String idx;
        if (timecode.isPresent()) {
            idx = timecode.get().toString().replace(':', '_');
        }
        else if (elapsedTime.isPresent()) {
            idx = elapsedTime.get().toMillis() + "";
        }
        else {
            Instant t = timestamp.orElse(Instant.now());
            idx = timeFormat.format(t);

        }
        return idx + "-" + image.getVideoReferenceUuid();
    }


}
